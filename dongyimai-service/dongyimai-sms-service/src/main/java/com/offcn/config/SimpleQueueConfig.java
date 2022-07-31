package com.offcn.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/18 15:50
 * @version: 1.0
 */

@Configuration
public class SimpleQueueConfig {

    /**
     * 定义简单队列名
     */
    public final static String SIMPLE_QUEUE = "dongyimai.sms.queue";

    @Bean
    public Queue simpleQueue() {
        return new Queue(SIMPLE_QUEUE);
    }
}

