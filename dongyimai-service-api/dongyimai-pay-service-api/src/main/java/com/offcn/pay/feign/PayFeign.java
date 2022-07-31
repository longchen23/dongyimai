package com.offcn.pay.feign;

import com.offcn.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/28 19:56
 * @version: 1.0
 */

@FeignClient(name = "pay")
@RequestMapping("/pay")
public interface PayFeign {

    @PostMapping("/closepay")
    Result closePay(@RequestParam("outTradeNo") Long outTradeNo) ;
}
