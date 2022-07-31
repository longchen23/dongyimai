package com.offcn.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.offcn.entity.Result;
import com.offcn.pay.feign.PayFeign;
import com.offcn.seckill.pojo.SeckillOrder;
import com.offcn.seckill.pojo.SeckillStatus;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.seckillordertimer}")
public class SeckillOrderDelayMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PayFeign payFeign;

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 读取消息
     * 判断Redis中是否存在对应的订单
     * 如果存在，则关闭支付，再关闭订单
     *
     * @param message
     */
    @RabbitHandler
    public void consumeMessage(@Payload String message) {
        //1、JSON解析消息成秒杀状态
        SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);
        System.out.println("===接收延时消息===: " + seckillStatus);
        //2、秒杀状态获取用户名
        String username = seckillStatus.getUsername();
        //3、从redis中获取秒杀订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //4、如果存在，则关闭支付和关闭订单回滚
        if (seckillOrder != null) {
            System.out.println("===准备回滚===: " + seckillStatus);
            try {
                Result result = payFeign.closePay(seckillOrder.getId());
                System.out.println("result: " + result.getData());
                Map<String, String> closeMap = (Map<String, String>) result.getData();
                System.out.println("closeMap:" + closeMap);
                //关闭订单
                seckillOrderService.closeOrder(username);
                System.out.println("回滚成功");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("回滚异常");
            }
        }
    }

}
