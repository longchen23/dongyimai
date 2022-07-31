package com.offcn.pay.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/28 17:28
 * @version: 1.0
 */

public class DirectQueueConfig {

    @Autowired
    private Environment env;

    //生成队列对象
    @Bean
    public Queue payDirectQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"));
    }


    //生产交换机对象
    @Bean
    public DirectExchange payDirectExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"));
    }

    //绑定关系对象
    @Bean
    public Binding payBindingDirectQueue(Queue payDirectQueue, DirectExchange payDirectExchange) {
        return BindingBuilder.bind(payDirectQueue).to(payDirectExchange).with(env.getProperty("mq.pay.routing.seckillorder"));
    }

}
