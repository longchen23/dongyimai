package com.offcn.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/28 19:16
 * @version: 1.0
 */

@Configuration
public class DirectQueueConfig {

    @Autowired
    private Environment env;

    //生成队列对象
    @Bean
    public Queue seckillDirectQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"),true);
    }

    //生产交换机对象
    @Bean
    public DirectExchange seckillDirectExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"));
    }

    //绑定关系对象
    @Bean
    public Binding seckillBindingDirectQueue(Queue seckillDirectQueue, DirectExchange seckillDirectExchange) {
        return BindingBuilder.bind(seckillDirectQueue).to(seckillDirectExchange).with(env.getProperty("mq.pay.routing.seckillorder"));
    }

}
