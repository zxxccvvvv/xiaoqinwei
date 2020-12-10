package com.atguigu.gmall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.tools.cache.AbstractIndexedFileCacheBacking;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Configuration
@Slf4j
public class RabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack){
                log.warn("消息没到达交换机");
            }

        });
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.warn("消息没有到达队列，来自于交换机：{}，路由键：{}，消息内容：{}", exchange, routingKey, new String(message.getBody()));
        });

    }
    /**
     * 定时关单
     * @return
     */

    @Bean
    public Queue ttlQueue(){
        return QueueBuilder.durable("ORDER_TTL_QUEUE")
                .withArgument("x-message-ttl", 90000)
                .withArgument("x-dead-letter-exchange", "ORDER_EXCHANGE")
                .withArgument("x-dead-letter-routing-key", "order.dead").build();
    }

    @Bean
    public Binding ttlBinding(){
        return new Binding("ORDER_TTL_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "order.ttl", null);
    }
    @Bean
    public Queue deadQueue(){
        return QueueBuilder.durable("ORDER_DEAD_QUEUE").build();
    }

    @Bean
    public Binding deadBinding(){
        return new Binding("ORDER_DEAD_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "order.dead", null);
    }

}
