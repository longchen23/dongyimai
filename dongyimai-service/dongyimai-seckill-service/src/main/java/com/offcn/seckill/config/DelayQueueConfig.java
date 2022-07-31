package com.offcn.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DelayQueueConfig {


    @Autowired
    private Environment env;


    //生成队列对象
    @Bean
    public Queue directQueue() {
        //durable 服务器重启，消息丢失。
        return new Queue(env.getProperty("mq.pay.queue.seckillordertimer"),true);
    }

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(env.getProperty("mq.pay.queue.seckillordertimerdelay"))
                .withArgument("x-dead-letter-exchange", env.getProperty("mq.pay.exchange.seckillordertimer"))
                .withArgument("x-dead-letter-routing-key", env.getProperty("mq.pay.queue.seckillordertimer"))
                .build();
    }

    //生产交换机对象
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillordertimer"));
    }

    //绑定关系对象
    @Bean
    public Binding bindingDirectQueue1(Queue directQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(directQueue).to(directExchange).with(env.getProperty("mq.pay.queue.seckillordertimer"));
    }
}
