package com.offcn.order.service;

import com.offcn.order.pojo.Cart;

import java.util.List;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/21 20:10
 * @version: 1.0
 */

public interface CartService {

    /**
     * 添加商品到购物车
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return cartList
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 从redis中查询购物车
     *
     * @param username
     * @return username
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     *
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username, List<Cart> cartList);

}
