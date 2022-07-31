package com.offcn.pay;

import com.offcn.utils.FeignInterceptor;
import com.offcn.utils.TokenDecode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/25 15:58
 * @version: 1.0
 */

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"com.offcn.order.feign"})
@EnableDiscoveryClient
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class);
    }

    @Bean
    public TokenDecode tokenDecode(){
        return new TokenDecode();
    }

    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}