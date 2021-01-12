package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.PmsFeignClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class PmsListener {

    @Autowired
    private PmsFeignClient pmsFeignClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    /**
     * 监听sku 价格变化
     */
    @RabbitListener(bindings = @QueueBinding(
          value = @Queue(value = "cart_spu_queue",durable = "true"),
            exchange = @Exchange(value = "item_exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "spu.update"
    ))
    public void spuPriceListener(Long spuId, Channel channel, Message message) throws IOException {
        try {
            List<SkuEntity> skuEntities = pmsFeignClient.querySkuBySpuId(spuId).getData();

            skuEntities.forEach(skuEntity -> {
                BigDecimal price = skuEntity.getPrice();

                redisTemplate.opsForValue().set(PRICE_PREFIX + skuEntity.getId(),price.toString());
            });

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            //是否已重试过
            if (message.getMessageProperties().getRedelivered()) {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            }
        }
    }

    /**
     * 监听订单支付完成后删除购物车中已支付商品的消息
     * @param map
     * @param channel
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart_order_queue",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = "cart.delete"
    ))
    public void orderListener(Map<String,Object> map,Channel channel,Message message) throws IOException {

        try {
            Long userId = (Long) map.get("userId");
            String skuIdString = map.get("skuIds").toString();
            List<String> skuIds = JSON.parseArray(skuIdString, String.class);

            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            hashOps.delete(skuIds.toArray());

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            //是否已重试过
            if (message.getMessageProperties().getRedelivered()) {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {

                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }
}
