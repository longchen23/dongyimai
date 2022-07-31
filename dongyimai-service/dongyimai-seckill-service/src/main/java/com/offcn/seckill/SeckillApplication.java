package com.offcn.seckill;

import com.offcn.utils.FeignInterceptor;
import com.offcn.utils.IdWorker;
import com.offcn.utils.TokenDecode;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.offcn.seckill.feign","com.offcn.pay.feign"})
@EnableScheduling//开启定时任务注解
@EnableAsync//开启异步
@MapperScan(basePackages = {"com.offcn.seckill.dao"})
public class SeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }
}