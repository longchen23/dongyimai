package com.offcn.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.entity.StatusCode;
import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.dao.SeckillOrderMapper;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.seckill.pojo.SeckillOrder;
import com.offcn.seckill.pojo.SeckillStatus;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.seckill.task.MultiThreadingCreateOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/****
 * @Author:ujiuye
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * SeckillOrder条件+分页查询
     *
     * @param seckillOrder 查询条件
     * @param page         页码
     * @param size         页大小
     * @return 分页结果
     */
    @Override
    public PageResult<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        Page<SeckillOrder> mypage = new Page<>(page, size);
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        IPage<SeckillOrder> iPage = this.page(mypage, queryWrapper);
        return new PageResult<>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * SeckillOrder分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<SeckillOrder> findPage(int page, int size) {
        Page<SeckillOrder> mypage = new Page<>(page, size);
        IPage<SeckillOrder> iPage = this.page(mypage, new QueryWrapper<>());

        return new PageResult<>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * SeckillOrder条件查询
     *
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        //构建查询条件
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * SeckillOrder构建查询对象
     *
     * @param seckillOrder
     * @return
     */
    public QueryWrapper<SeckillOrder> createQueryWrapper(SeckillOrder seckillOrder) {
        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<>();
        if (seckillOrder != null) {
            // 主键
            if (!StringUtils.isEmpty(seckillOrder.getId())) {
                queryWrapper.eq("id", seckillOrder.getId());
            }
            // 秒杀商品ID
            if (!StringUtils.isEmpty(seckillOrder.getSeckillId())) {
                queryWrapper.eq("seckill_id", seckillOrder.getSeckillId());
            }
            // 支付金额
            if (!StringUtils.isEmpty(seckillOrder.getMoney())) {
                queryWrapper.eq("money", seckillOrder.getMoney());
            }
            // 用户
            if (!StringUtils.isEmpty(seckillOrder.getUserId())) {
                queryWrapper.eq("user_id", seckillOrder.getUserId());
            }
            // 商家
            if (!StringUtils.isEmpty(seckillOrder.getSellerId())) {
                queryWrapper.eq("seller_id", seckillOrder.getSellerId());
            }
            // 创建时间
            if (!StringUtils.isEmpty(seckillOrder.getCreateTime())) {
                queryWrapper.eq("create_time", seckillOrder.getCreateTime());
            }
            // 支付时间
            if (!StringUtils.isEmpty(seckillOrder.getPayTime())) {
                queryWrapper.eq("pay_time", seckillOrder.getPayTime());
            }
            // 状态
            if (!StringUtils.isEmpty(seckillOrder.getStatus())) {
                queryWrapper.eq("status", seckillOrder.getStatus());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(seckillOrder.getReceiverAddress())) {
                queryWrapper.eq("receiver_address", seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if (!StringUtils.isEmpty(seckillOrder.getReceiverMobile())) {
                queryWrapper.eq("receiver_mobile", seckillOrder.getReceiverMobile());
            }
            // 收货人
            if (!StringUtils.isEmpty(seckillOrder.getReceiver())) {
                queryWrapper.eq("receiver", seckillOrder.getReceiver());
            }
            // 交易流水
            if (!StringUtils.isEmpty(seckillOrder.getTransactionId())) {
                queryWrapper.eq("transaction_id", seckillOrder.getTransactionId());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    /**
     * 修改SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {
        this.updateById(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder) {
        this.save(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id) {
        return this.getById(id);
    }

    /**
     * 查询SeckillOrder全部数据
     *
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return this.list(new QueryWrapper<>());
    }

    /**
     * 添加订单
     *
     * @param id
     * @param time
     * @param username
     */
    @Override
    public void
    add(Long id, String time, String username) {
        //判断是否排对
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        System.out.println("userQueueCount:" + userQueueCount);
        if (userQueueCount > 1) {
            //如果抢单数量大于1，表示重复抢单
            throw new RuntimeException(String.valueOf(StatusCode.REPERROR));
        }
        //排队信息封装
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);
        //存入redis中，使用list存储
        redisTemplate.boundListOps("SeckillStatusQueue").leftPush(seckillStatus);
        //将抢单状态存入redis中
        redisTemplate.boundHashOps("UserSeckillStatus").put(username, seckillStatus);
        //多线程操作
        multiThreadingCreateOrder.createOrder();
    }

    /**
     * 抢单状态查询
     *
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserSeckillStatus").get(username);
    }

    /**
     * 更新订单状态
     *
     * @param outTradeNo
     * @param transactionId
     * @param username
     */
    @Override
    public void updatePayStatus(String outTradeNo, String transactionId, String username) {
        //从Redis数据库查询出来订单数据
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //修改状态
        assert seckillOrder != null;
        seckillOrder.setStatus("1");
        //支付时间
        seckillOrder.setPayTime(new Date());
        //设置交易流水号
        seckillOrder.setTransactionId(transactionId);
        //同步到数据库中
        seckillOrderMapper.insert(seckillOrder);
        //清空redis中此用户订单
        redisTemplate.boundHashOps("SeckillOrder").delete(username);
        //清空排队信息
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //删除抢购状态信息
        redisTemplate.boundHashOps("UserSeckillStatus").delete(username);
    }

    /**
     * 关闭订单，回滚库存
     */
    @Override
    public void closeOrder(String username) {
        //将消息转换成SeckillStatus
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserSeckillStatus").get(username);
        //获取redis中订单信息
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //如果Redis中有订单信息，说明用户未支付
        if (seckillStatus != null && seckillOrder != null) {
            //删除订单
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            //回滚库存
            //1.从Redis中获取该商品
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
            //2.如果Redis中没有，则从数据库中加载
            if (seckillGoods == null) {
                seckillGoods = seckillGoodsMapper.selectById(seckillStatus.getGoodsId());
            }
            //3.数量+1  (递增数量+1，队列数量+1)
            Long surplusCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), 1);
            seckillGoods.setStockCount(surplusCount.intValue());
            redisTemplate.boundListOps("SeckillGoodsCountQueue_" + seckillStatus.getGoodsId()).leftPush(seckillStatus.getGoodsId());

            //4.数据同步到Redis中
            redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).put(seckillStatus.getGoodsId(), seckillGoods);
            //清理排队
            redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());
            //清理抢单
            redisTemplate.boundHashOps("UserSeckillStatus").delete(seckillStatus.getUsername());
        }
    }

}
