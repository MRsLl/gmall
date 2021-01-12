package com.atguigu.gmall.oms.config;

import net.bytebuddy.implementation.bind.annotation.Argument;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMqConfig {

    //声明延时交换机 ORDER-EXCHANGE

    //声明延时队列 order-ttl-queue
    @Bean
    public Queue ttlQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 60000);
        arguments.put("x-dead-letter-exchange", "ORDER-EXCHANGE");
        arguments.put("x-dead-letter-routing-key", "order.dead");

        return new Queue("order-ttl-queue", true, false, false, arguments);
    }

    //绑定延时队列到延时交换机
    @Bean
    public Binding ttlBinding() {
        return new Binding("order-ttl-queue", Binding.DestinationType.QUEUE,"ORDER-EXCHANGE","order.create",null);
    }

    //声明死信交换机 ORDER-EXCHANGE

    //声明死信队列 order-dead-queue
    @Bean
    public Queue deadQueue() {
        return new Queue("order-dead-queue",true,false,false);
    }

    //绑定死信队列给死信交换机
    @Bean
    public Binding deadBinding() {
        return new Binding("order-dead-queue", Binding.DestinationType.QUEUE,"ORDER-EXCHANGE","order.dead",null);
    }
}
