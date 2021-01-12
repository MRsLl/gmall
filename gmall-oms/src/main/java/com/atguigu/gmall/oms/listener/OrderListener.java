package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class OrderListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Resource
    private OrderMapper orderMapper;

    @RabbitListener(queues = "order-dead-queue")
    public void closeOrder(String orderToken, Message message, Channel channel) throws IOException {
        try {
            //若更新订单为关闭成功，则发送消息解锁库存
            if (orderMapper.updateOrderStatus(orderToken,0,4) == 1) {
                rabbitTemplate.convertAndSend("ORDER-EXCHANGE","stock.ttl", orderToken);
            }
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
