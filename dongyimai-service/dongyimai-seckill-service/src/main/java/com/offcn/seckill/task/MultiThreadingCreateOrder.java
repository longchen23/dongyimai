package com.offcn.seckill.task;

import com.alibaba.fastjson.JSON;
import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.seckill.pojo.SeckillOrder;
import com.offcn.seckill.pojo.SeckillStatus;
import com.offcn.utils.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/27 20:18
 * @version: 1.0
 */

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    /**
     * 多线程操作下单
     */
    @Async
    public void createOrder() {
        //从redis中取出队列信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillStatusQueue").rightPop();
        System.out.println("seckillStatus: " + seckillStatus);
        try {
            //判断队列中是否有商品数据，没有则商品售罄
            assert seckillStatus != null;
            Object sgood = redisTemplate.boundListOps("SeckillGoodsCountQueue_" + seckillStatus.getGoodsId()).rightPop();
            if (sgood == null) {
                //清理当前用户的排队信息
                clearQueue(seckillStatus);
                return;
            }
            //时间区间
            String time = seckillStatus.getTime();
            //用户登录名
            String username = seckillStatus.getUsername();
            //用户抢购商品
            Long id = seckillStatus.getGoodsId();
            //获取商品数据
            SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            System.out.println("goods: " + goods);
            if (goods == null) {
                throw new RuntimeException("已售罄");
            }
            SeckillOrder order = new SeckillOrder();
            order.setId(idWorker.nextId());
            order.setSeckillId(id);
            order.setMoney(goods.getCostPrice());
            order.setUserId(username);
            order.setCreateTime(new Date());
            order.setStatus("0");
            //将秒杀订单存入redis中
            redisTemplate.boundHashOps("SeckillOrder").put(username, order);
            //商品库存减一
            Long surplusCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, -1);
            assert surplusCount != null;
            goods.setStockCount(surplusCount.intValue());
            if (surplusCount <= 0) {
                //如果商品库存已清空则将数据同步到数据库中
                seckillGoodsMapper.updateById(goods);
                //没有库存清空redis
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
            } else {
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, goods);
            }
            //更新抢单成功后状态
            seckillStatus.setStatus(2);
            seckillStatus.setOrderId(order.getId());
            seckillStatus.setMoney(order.getMoney().floatValue());
            redisTemplate.boundHashOps("UserSeckillStatus").put(username, seckillStatus);
            //发送延时消息到MQ中
            sendTimerMessage(seckillStatus);
            System.out.println("===发送延时队列===: " + seckillStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送延时消息到RabbitMQ中
     *
     * @param seckillStatus
     */
    private void sendTimerMessage(SeckillStatus seckillStatus) {
        String jsonString = JSON.toJSONString(seckillStatus);
        rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"), (Object) jsonString, message -> {
            message.getMessageProperties().setExpiration("10000");
            return message;
        });
    }

    /**
     * 清理用户排队信息
     *
     * @param seckillStatus
     */
    private void clearQueue(SeckillStatus seckillStatus) {
        //清理排队队列
        redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());
        //清理抢单队列
        redisTemplate.boundHashOps("UserSeckillStatus").delete(seckillStatus.getUsername());
    }
}
