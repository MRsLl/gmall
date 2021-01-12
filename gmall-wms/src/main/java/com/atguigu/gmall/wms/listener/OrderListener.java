package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Component
public class OrderListener {

    private static final String KEY_PREFIX = "store:lock:";
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private WareSkuMapper wareSkuMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order_stock_queue", durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = "stock.unlock"
    ))
    public void stockUnlockListener(String orderToken, Channel channel, Message message) throws IOException {

        try {
            //1.从redis 中获取缓存的锁定商品库存信息
            String skuLockVosJson = redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);

            //2.若库存信息非空，则解锁库存并删除redis 中的缓存信息
            if (StringUtils.isNotBlank(skuLockVosJson)) {
                List<SkuLockVo> skuLockVos = JSON.parseArray(skuLockVosJson, SkuLockVo.class);
                skuLockVos.forEach(skuLockVo -> {
                    wareSkuMapper.tryUnLock(skuLockVo.getWareSkuId(), skuLockVo.getCount());
                });

                redisTemplate.delete(KEY_PREFIX + orderToken);
            }
            //3.确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

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
