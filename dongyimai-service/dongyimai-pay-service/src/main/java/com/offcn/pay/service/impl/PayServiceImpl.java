package com.offcn.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/25 15:58
 * @version: 1.0
 */

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private AlipayClient alipayClient;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Override
    public Map<String, String> createNative(Map<String, String> parameters) {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(notifyUrl);
        long totalFee = Long.parseLong(parameters.get("totalFee"));
        BigDecimal bigFen = BigDecimal.valueOf(totalFee);
        BigDecimal cs = BigDecimal.valueOf(100d);
        BigDecimal bigYuan = bigFen.divide(cs);
        System.out.println("预下单金额:" + bigYuan.doubleValue());
        JSONObject bizContent = new JSONObject();
        //商户订单号，设置最晚付款时间
        bizContent.put("out_trade_no", parameters.get("outTradeNo"));
        bizContent.put("total_amount", bigYuan.doubleValue());
        bizContent.put("subject", "测试商品");
        bizContent.put("store_id", "taiba");
        bizContent.put("timeout_express", "90m");
        //设置body mq消息队列
        bizContent.put("body", "queue=" + parameters.get("queue") +
                "&username=" + parameters.get("username") +
                "&routing=" + parameters.get("routing") +
                "&exchange=" + parameters.get("exchange"));
        request.setBizContent(bizContent.toString());
        Map<String, String> resultMap = null;
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            System.out.println(response.getCode());
            System.out.println(response.getBody());
            if ("10000".equals(response.getCode())) {
                resultMap = new HashMap<>();
                resultMap.put("qrcode", response.getQrCode());
                resultMap.put("outTradeNo", response.getOutTradeNo());
                resultMap.put("totalFee", parameters.get("totalFee"));
                System.out.println("创建订单qrcode:" + response.getQrCode());
                System.out.println("创建订单outTradeNo:" + response.getOutTradeNo());
                System.out.println("创建订单totalFee:" + parameters.get("totalFee"));
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 交易查询接口alipay.trade.query：
     * 获取指定订单编号的，交易状态
     *
     * @throws AlipayApiException
     */
    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        //创建API对应的request类
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + outTradeNo + "\"," +
                "\"trade_no\":\"\"}"); //设置业务参数
        Map<String, String> resultMap = null;
        try {
            //通过alipayClient调用API，获得对应的response类
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            System.out.println(response.getCode());
            System.out.println(response.getBody());
            if ("10000".equals(response.getCode())) {
                resultMap = new HashMap<>();
                resultMap.put("outTradeNo", response.getOutTradeNo());
                resultMap.put("tradeNo", response.getOutTradeNo());
                resultMap.put("tradeStatus", response.getTradeStatus());
                System.out.println("支付订单outTradeNo:" + response.getOutTradeNo());
                System.out.println("支付订单tradeNo:" + response.getOutTradeNo());
                System.out.println("支付订单tradeStatus:" + response.getTradeStatus());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 关闭订单
     *
     * @param outTradeNo
     * @return
     */
    @Override
    public Map<String, String> closePay(Long outTradeNo){
        //创建API对应的request类
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + outTradeNo + "\"," +
                "\"trade_no\":\"\"}"); //设置业务参数
        Map<String, String> resultMap = null;//结果集
        try{
            //通过alipayClient调用API，获得对应的response类
            AlipayTradeCancelResponse response = alipayClient.execute(request);
            resultMap = new HashMap<>();
            System.out.println("状态码code:" + response.getCode());
            System.out.println("body:" + response.getBody());
            //如果是10000则表示连接到支付宝平台
            if ("10000".equals(response.getCode())) {
                //存储数据
                resultMap.put("tradeNo", response.getTradeNo());
                //获取流水号
                resultMap.put("outTradeNo", response.getOutTradeNo());
                System.out.println("关闭订单tradeNo:" + response.getTradeNo());
                System.out.println("关闭订单outTradeNo:" + response.getOutTradeNo());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

}
