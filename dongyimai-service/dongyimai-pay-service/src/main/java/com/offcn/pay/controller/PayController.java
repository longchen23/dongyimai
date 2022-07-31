package com.offcn.pay.controller;

import com.alibaba.fastjson.JSON;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.order.feign.OrderFeign;
import com.offcn.order.pojo.PayLog;
import com.offcn.pay.service.PayService;
import com.offcn.utils.TokenDecode;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/25 15:58
 * @version: 1.0
 */

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private PayService payService;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private TokenDecode tokenDecode;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 生成二维码
     *
     * @return
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative() {
        //IdWorker idworker = new IdWorker();
        //return payService.createNative(idworker.nextId() + "", "1");
        //获取用户名
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        //从redis中查询支付日志
        Result<PayLog> payLogResult = orderFeign.searchPayLogFromRedis(username);
        if (payLogResult != null) {
            PayLog payLog = payLogResult.getData();
            Map<String, String> parameters = new HashMap<>();
            parameters.put("outTradeNo", payLog.getOutTradeNo());
            parameters.put("totalFee", payLog.getTotalFee() + "");
            return payService.createNative(parameters);
        }
        return new HashMap<>();
    }

    /**
     * 创建二维码连接地址返回给前端 生成二维码图片
     *
     * @param parameters 包含 订单号  包含 金额  包含 queue队列名称 交换机信息 路由信息 用户名
     * @return parameters
     */
    @GetMapping("/create/native")
    public Result<Map<String, String>> createNative(@RequestParam Map<String, String> parameters) {
        //获取用户名
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        parameters.put("username", username);
        Map<String, String> resultMap = payService.createNative(parameters);
        return new Result<>(true, StatusCode.OK, "二维码连接地址创建成功", resultMap);
    }

    /**
     * 接收 支付通知的结果
     */
    @RequestMapping("/notify/url")
    public String getResult(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> attributeNames = request.getParameterNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            map.put(attributeName, request.getParameter(attributeName));
        }
        String jsonString = JSON.toJSONString(map);
        System.out.println("jsonString: " + jsonString);
        String body = map.get("body");
        if (!StringUtils.isEmpty(body)) {
            String[] splits = body.split("&");
            Map<String, String> bodyMap = new HashMap<>();
            for (String split : splits) {
                String[] vs = split.split("=");
                bodyMap.put(vs[0], vs[1]);
            }
            //发送消息 交换机及路由
            rabbitTemplate.convertAndSend(bodyMap.get("exchange"), bodyMap.get("routing"), jsonString);
        }
        return "pay-success";
    }

    /**
     * 查询支付状态
     *
     * @param outTradeNo
     * @return
     */
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo) {
        Result result;
        int i = 0;
        while (true) {
            Map<String, String> map = payService.queryPayStatus(outTradeNo);
            if (map == null) {
                result = new Result<>(false, StatusCode.ERROR, "支付出错");
                break;
            }
            String tradeStatus = map.get("tradeStatus");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                result = new Result<>(true, StatusCode.OK, "支付成功");
                orderFeign.updateOrderStatus(map.get("outTradeNo"), map.get("tradeNo"));
                break;
            }
            if ("TRADE_CLOSED".equals(tradeStatus)) {
                result = new Result<>(true, StatusCode.OK, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if ("TRADE_FINISHED".equals(tradeStatus)) {
                result = new Result<>(true, StatusCode.OK, "交易结束，不可退款");
                orderFeign.updateOrderStatus(map.get("outTradeNo"), map.get("tradeNo"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
            if (i >= 100) {
                result = new Result<>(false, StatusCode.ERROR, "二维码超时");
                break;
            }
        }
        return result;
    }

    /**
     * 关闭支付
     *
     * @param outTradeNo
     * @return
     */
    @PostMapping("closepay")
    public Result closePay(Long outTradeNo) {
        try {
            Map<String, String> resultMap = payService.closePay(outTradeNo);
            return new Result<>(true, StatusCode.OK, "订单关闭成功", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "订单关闭失败");
        }
    }

}