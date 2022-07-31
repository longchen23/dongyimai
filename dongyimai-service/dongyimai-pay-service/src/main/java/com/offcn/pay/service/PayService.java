package com.offcn.pay.service;

import java.util.Map;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/25 15:58
 * @version: 1.0
 */

public interface PayService {

    /**
     * 生成支付宝支付二维码
     *
     * @param parameters
     * @return parameters
     */
    Map<String, String> createNative(Map<String, String> parameters);

    /**
     * 查询支付状态
     *
     * @param outTradeNo
     */
    Map<String, String> queryPayStatus(String outTradeNo);

    /**
     * 关闭支付
     *
     * @param outTradeNo
     * @return
     */
    Map<String, String> closePay(Long outTradeNo);

}