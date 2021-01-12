package com.atguigu.gmall.wms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMqConfig {

    //声明延时交换机 ORDER-EXCHANGE

    //声明延时队列 stock-ttl-queue
    @Bean
    public Queue ttlQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 60000);
        arguments.put("x-dead-letter-exchange", "ORDER-EXCHANGE");
        arguments.put("x-dead-letter-routing-key", "stock.unlock");

        return new Queue("stock-ttl-queue", true, false, false, arguments);
    }

    //绑定延时队列到延时交换机
    @Bean
    public Binding ttlBinding() {
        return new Binding("stock-ttl-queue", Binding.DestinationType.QUEUE,"ORDER-EXCHANGE","stock.ttl",null);
    }

    //声明死信交换机 ORDER-EXCHANGE

    //声明死信队列 借用 order_stock_queue

    //把死信队列绑定到死信交换机 监听器中已绑定

}
