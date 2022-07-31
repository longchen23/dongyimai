package com.offcn.order;

import com.offcn.utils.FeignInterceptor;
import com.offcn.utils.IdWorker;
import com.offcn.utils.TokenDecode;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
=======
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
>>>>>>> 6c98102 (第二次提交)
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.offcn.order.dao")
<<<<<<< HEAD
@EnableFeignClients(basePackages = {"com.offcn.sellergoods.feign", "com.offcn.user.feign"})
=======
@EnableFeignClients(basePackages = {"com.offcn.sellergoods.feign","com.offcn.user.feign"})
>>>>>>> 6c98102 (第二次提交)
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    /**
     * 创建拦截器Bean对象
     *
     * @return feignInterceptor
     */
    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }

    @Bean
    public TokenDecode getTokenDecode() {
        return new TokenDecode();
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

}