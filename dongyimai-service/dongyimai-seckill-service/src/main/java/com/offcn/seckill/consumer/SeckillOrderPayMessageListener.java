package com.offcn.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/28 19:19
 * @version: 1.0
 */

@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillOrderPayMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 监听消费消息
     *
     * @param message
     */
    @RabbitHandler
    public void consumeMessage(@Payload String message) {
        System.out.println(message);
        //将消息转换成Map对象
        Map<String, String> resultMap = JSON.parseObject(message, Map.class);
        System.out.println("监听到的消息:" + resultMap);
        //获取交易状态
        String tradeStatus = resultMap.get("trade_status");
        //判断交易状态是否等于成功
        if (tradeStatus != null && tradeStatus.equalsIgnoreCase("TRADE_SUCCESS")) {
            String body = resultMap.get("body");
            Map<String, String> bodyMap = new HashMap<>();
            if (resultMap.get("body") != null) {
                String[] splits = body.split("&");
                for (String split : splits) {
                    String[] vs = split.split("=");
                    bodyMap.put(vs[0], vs[1]);
                }
            }
            seckillOrderService.updatePayStatus(resultMap.get("out_trade_no"), resultMap.get("trade_no"), bodyMap.get("username"));
        } else {
            String body = resultMap.get("body");
            Map<String, String> bodyMap = new HashMap<>();
            if (resultMap.get("body") != null) {
                String[] splits = body.split("&");
                for (String split : splits) {
                    String[] vs = split.split("=");
                    bodyMap.put(vs[0], vs[1]);
                }
            }
            seckillOrderService.closeOrder(resultMap.get("username"));
        }
    }
}

