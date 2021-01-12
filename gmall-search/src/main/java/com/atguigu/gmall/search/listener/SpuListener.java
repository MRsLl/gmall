package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.search.sevice.SearchService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SpuListener {

    @Autowired
    private SearchService searchService;

    /**
     * 处理insert的消息
     *
     * @param id
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "item_spu_queue", durable = "true"),
            exchange = @Exchange(
                    value = "item_exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert"}))
    public void listenCreate(Long id, String msg, Channel channel, Message message) throws Exception {
        if (id == null) {
            return;
        }
        try {
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            // 创建索引
            this.searchService.createIndex(id);
        } catch (Exception e) {
            e.printStackTrace();
            // 是否已经重试过
            if (message.getMessageProperties().getRedelivered()){
                // 已重试过直接拒绝
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } else {
                // 未重试过，重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        }
    }
}
