package com.offcn.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.order.dao.OrderItemMapper;
import com.offcn.order.dao.OrderMapper;
import com.offcn.order.dao.PayLogMapper;
import com.offcn.order.pojo.Cart;
import com.offcn.order.pojo.Order;
import com.offcn.order.pojo.OrderItem;
import com.offcn.order.pojo.PayLog;
import com.offcn.order.service.OrderService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.user.feign.UserFeign;
import com.offcn.utils.IdWorker;
import feign.Feign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/****
 * @Author:ujiuye
 * @Description:Order业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ItemFeign itemFeign;

    @Autowired
    private UserFeign userFeign;

    @Resource
    private PayLogMapper payLogMapper;

    /**
     * Order条件+分页查询
     *
     * @param order 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Order> findPage(Order order, int page, int size) {
        Page<Order> mypage = new Page<>(page, size);
        QueryWrapper<Order> queryWrapper = this.createQueryWrapper(order);
        IPage<Order> iPage = this.page(mypage, queryWrapper);
        return new PageResult<>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Order分页查询
     *
     * @param page
     * @param size
     * @return 分页结果
     */
    @Override
    public PageResult<Order> findPage(int page, int size) {
        Page<Order> mypage = new Page<>(page, size);
        IPage<Order> iPage = this.page(mypage, new QueryWrapper<Order>());

        return new PageResult<>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Order条件查询
     *
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order) {
        //构建查询条件
        QueryWrapper<Order> queryWrapper = this.createQueryWrapper(order);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Order构建查询对象
     *
     * @param order
     * @return order
     */
    public QueryWrapper<Order> createQueryWrapper(Order order) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (order != null) {
            // 订单id
            if (!StringUtils.isEmpty(order.getOrderId())) {
                queryWrapper.eq("order_id", order.getOrderId());
            }
            // 实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分
            if (!StringUtils.isEmpty(order.getPayment())) {
                queryWrapper.eq("payment", order.getPayment());
            }
            // 支付类型，1、在线支付，2、货到付款
            if (!StringUtils.isEmpty(order.getPayType())) {
                queryWrapper.eq("pay_type", order.getPayType());
            }
            // 邮费。精确到2位小数;单位:元。如:200.07，表示:200元7分
            if (!StringUtils.isEmpty(order.getPostFee())) {
                queryWrapper.eq("post_fee", order.getPostFee());
            }
            // 状态：0:未完成 1:已完成 2:已退货
            if (!StringUtils.isEmpty(order.getOrderStatus())) {
                queryWrapper.eq("order_status", order.getOrderStatus());
            }
            // 订单创建时间
            if (!StringUtils.isEmpty(order.getCreateTime())) {
                queryWrapper.eq("create_time", order.getCreateTime());
            }
            // 订单更新时间
            if (!StringUtils.isEmpty(order.getUpdateTime())) {
                queryWrapper.eq("update_time", order.getUpdateTime());
            }
            // 付款时间
            if (!StringUtils.isEmpty(order.getPaymentTime())) {
                queryWrapper.eq("payment_time", order.getPaymentTime());
            }
            // 发货时间
            if (!StringUtils.isEmpty(order.getConsignTime())) {
                queryWrapper.eq("consign_time", order.getConsignTime());
            }
            // 交易完成时间
            if (!StringUtils.isEmpty(order.getEndTime())) {
                queryWrapper.eq("end_time", order.getEndTime());
            }
            // 交易关闭时间
            if (!StringUtils.isEmpty(order.getCloseTime())) {
                queryWrapper.eq("close_time", order.getCloseTime());
            }
            // 物流名称
            if (!StringUtils.isEmpty(order.getShippingName())) {
                queryWrapper.eq("shipping_name", order.getShippingName());
            }
            // 物流单号
            if (!StringUtils.isEmpty(order.getShippingCode())) {
                queryWrapper.eq("shipping_code", order.getShippingCode());
            }
            // 用户id
            if (!StringUtils.isEmpty(order.getUsername())) {
                queryWrapper.like("username", order.getUsername());
            }
            // 买家留言
            if (!StringUtils.isEmpty(order.getBuyerMessage())) {
                queryWrapper.eq("buyer_message", order.getBuyerMessage());
            }
            // 买家昵称
            if (!StringUtils.isEmpty(order.getBuyerNick())) {
                queryWrapper.eq("buyer_nick", order.getBuyerNick());
            }
            // 买家是否已经评价
            if (!StringUtils.isEmpty(order.getBuyerRate())) {
                queryWrapper.eq("buyer_rate", order.getBuyerRate());
            }
            // 收货人地区名称(省，市，县)街道
            if (!StringUtils.isEmpty(order.getReceiverAreaName())) {
                queryWrapper.eq("receiver_area_name", order.getReceiverAreaName());
            }
            // 收货人手机
            if (!StringUtils.isEmpty(order.getReceiverMobile())) {
                queryWrapper.eq("receiver_mobile", order.getReceiverMobile());
            }
            // 收货人邮编
            if (!StringUtils.isEmpty(order.getReceiverZipCode())) {
                queryWrapper.eq("receiver_zip_code", order.getReceiverZipCode());
            }
            // 收货人
            if (!StringUtils.isEmpty(order.getReceiverContact())) {
                queryWrapper.eq("receiver_contact", order.getReceiverContact());
            }
            // 过期时间，定期清理
            if (!StringUtils.isEmpty(order.getExpire())) {
                queryWrapper.eq("expire", order.getExpire());
            }
            // 发票类型(普通发票，电子发票，增值税发票)
            if (!StringUtils.isEmpty(order.getInvoiceType())) {
                queryWrapper.eq("invoice_type", order.getInvoiceType());
            }
            // 订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
            if (!StringUtils.isEmpty(order.getSourceType())) {
                queryWrapper.eq("source_type", order.getSourceType());
            }
            // 商家ID
            if (!StringUtils.isEmpty(order.getSellerId())) {
                queryWrapper.eq("seller_id", order.getSellerId());
            }
            // 总金额
            if (!StringUtils.isEmpty(order.getTotalMoney())) {
                queryWrapper.eq("total_money", order.getTotalMoney());
            }
            // 实际支付金额
            if (!StringUtils.isEmpty(order.getPayMoney())) {
                queryWrapper.eq("pay_money", order.getPayMoney());
            }
            // 总数量
            if (!StringUtils.isEmpty(order.getTotalNum())) {
                queryWrapper.eq("total_num", order.getTotalNum());
            }
            // 
            if (!StringUtils.isEmpty(order.getPreMoney())) {
                queryWrapper.eq("pre_money", order.getPreMoney());
            }
            // 支付状态 0:未支付 1:已支付 2:支付失败
            if (!StringUtils.isEmpty(order.getPayStatus())) {
                queryWrapper.eq("pay_status", order.getPayStatus());
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
     * 修改Order
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        this.updateById(order);
    }

    /**
     * 增加Order
     *
     * @param order
     */
    @GlobalTransactional(name = "default",rollbackFor = Exception.class)
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void add(Order order) {
        String username = order.getUsername();
        //得到购物车信息
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        List<String> orderIdList = new ArrayList<>();
        //设置初始支付金额
        double totalFee = 0;
        assert cartList != null;
        for (Cart cart : cartList) {
            //每个购物车对应一个订单
            long orderId = idWorker.nextId();
            order.setOrderId(orderId);
            System.out.println("orderId:" + orderId);
            //System.out.println("orderId:" + orderId);
            //记录到订单编号串中
            orderIdList.add(orderId + "");
            order.setPayType(order.getPayType());
            //未支付
            order.setPayStatus("0");
            //未完成
            order.setOrderStatus("0");
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            order.setReceiverAreaName(order.getReceiverAreaName());
            order.setReceiverContact(order.getReceiverContact());
            order.setReceiverMobile(order.getReceiverMobile());
            order.setSourceType("2");
            order.setSellerId(cart.getSellerId());
            //计算订单总金额=商品小计的总和
            //设置初始值
            double money = 0;
            for (OrderItem orderItem : cart.getOrderItemList()) {
                long id = idWorker.nextId();
                System.out.println("orderItemId:" + id);
                //System.out.println("orderItemId:" + id);
                orderItem.setId(id);
                orderItem.setOrderId(orderId);
                orderItemMapper.insert(orderItem);
                money += orderItem.getTotalFee().doubleValue();
            }
            System.out.println("添加订单完毕");
            totalFee += money;
            order.setPayment(BigDecimal.valueOf(money));
            System.out.println(order);
            this.save(order);
        }
        //判断支付方式是支付宝支付（在线支付）
        if ("1".equals(order.getPayType())) {
            PayLog payLog = new PayLog();
            //设置支付编号
            long outTradeNo = idWorker.nextId();
            payLog.setOutTradeNo(outTradeNo + "");
            //设置创建日志时间
            payLog.setCreateTime(new Date());
            //设置支付金额，有元转分
            BigDecimal bigDecimal = BigDecimal.valueOf(totalFee);
            BigDecimal multiply = bigDecimal.multiply(BigDecimal.valueOf(100d));
            payLog.setTotalFee(multiply.longValue());
            payLog.setUserId(username);
            //设置支付状态，初始为0
            payLog.setTradeState("0");
            //设置支付订单编号串
            String orderIdsString = orderIdList.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(" ", "");
            payLog.setOrderList(orderIdsString);
            //设置支付类型，设置为支付宝支付为1
            payLog.setPayType("1");
            //写入到数据库
            payLogMapper.insert(payLog);
            //写入到redis中
            redisTemplate.boundHashOps("payLog").put(username, payLog);
        }
        //添加积分
        userFeign.addPoints(10);
        //减少库存
        itemFeign.decrCount(username);
        String xID = RootContext.getXID();
        if (xID!=null) {
            throw new RuntimeException("出现异常");
        }
        System.out.println("order微服务事务ID: " + xID);

        //添加积分
        userFeign.addPoints(10);
        System.out.println("添加积分完毕");
        //减少库存
        itemFeign.decrCount(username);
        System.out.println("库存减少完毕");
        //清空购物车
        redisTemplate.boundHashOps("cartList").delete(username);
    }

    /**
     * 根据ID查询Order
     *
     * @param id
     * @return id
     */
    @Override
    public Order findById(Long id) {
        return this.getById(id);
    }

    /**
     * 查询Order全部数据
     *
     * @return order
     */
    @Override
    public List<Order> findAll() {
        return this.list(new QueryWrapper<>());
    }

    /**
     * 根据用户查询payLog
     *
     * @param userId
     * @return
     */
    @Override
    public PayLog searchPayLogFromRedis(String userId) {
        return (PayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    /**
     * 修改订单状态
     *
     * @param outTradeNo    支付订单号
     * @param transactionId 支付宝返回的交易流水号
     */
    @Override
    public void updateOrderStatus(String outTradeNo, String transactionId) {
        //修改tb_pay_log中支付状态
        PayLog payLog = payLogMapper.selectById(outTradeNo);
        //设置支付时间
        payLog.setPayTime(new Date());
        //设置支付流水号
        payLog.setTransactionId(transactionId);
        //设置支付状态
        payLog.setTradeState("1");
        payLogMapper.updateById(payLog);
        //修改tb_order中支付状态
        String orderList = payLog.getOrderList();
        String[] split = orderList.split(",");
        for (String orderId : split) {
            Order order = this.getById(orderId);
            order.setPayStatus("1");
            this.updateById(order);
        }
        //清空redis中支付订单
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}
