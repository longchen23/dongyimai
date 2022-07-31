package com.offcn.order.service.impl;

import com.offcn.entity.Result;
import com.offcn.order.pojo.Cart;
import com.offcn.order.pojo.OrderItem;
import com.offcn.order.service.CartService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/21 20:11
 * @version: 1.0
 */

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemFeign itemFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        Result<Item> itemResult = itemFeign.findById(itemId);
        Item item = itemResult.getData();
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家id
        String sellerId = item.getSellerId();
        //3.通过商家id判断是否有该商家的购物车信息
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果不存在就新建
        if (cart == null) {
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            OrderItem orderItem = createOrderItem(item, num);
            List orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            //判断购物车明细列表中是否存在该商品
            OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {
                //如果没有则新增
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //存在就在原有基础上修改数量及金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
                //如果商品数量小于1则移除
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果移除后的商品明细等于，则移除该商家的购物车
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItemList, Long itemId) {
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    private OrderItem createOrderItem(Item item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(num)));
        return orderItem;
    }

    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }
}
