package com.offcn.order.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.order.pojo.Cart;
import com.offcn.order.service.CartService;
import com.offcn.utils.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenDecode tokenDecode;

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        System.out.println("username:" + username);
        return cartService.findCartListFromRedis(username);//从redis中提取
    }


    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //String username = "ujiuye";
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        System.out.println("username:" + username);
        try {
            List<Cart> cartList = findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            cartService.saveCartListToRedis(username, cartList);
            return new Result(true, StatusCode.OK, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "添加失败");
        }

    }
}