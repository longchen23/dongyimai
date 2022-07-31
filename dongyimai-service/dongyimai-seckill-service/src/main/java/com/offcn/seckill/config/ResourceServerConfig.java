package com.offcn.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
@EnableResourceServer //开启资源服务器
//@EnableGlobalMethodSecurity:开启全局方法安全管理,prePostEnabled:启用四个方法注解,securedEnabled:安全管理
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    private static final String PUBLIC_KEY = "public.key";

    //存储令牌并解密问题
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        //使用公钥对jwt进行鉴权
        converter.setVerifierKey(getPubKey());
        return converter;
    }

    //读取public.key中公钥
    private String getPubKey() {
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            //字节流转字符流，使用转换流
            InputStreamReader isr = new InputStreamReader(resource.getInputStream());
            //高效字符流
            BufferedReader red = new BufferedReader(isr);
            return red.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //用于对请求路径进行控制
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()//设置认证请求
                .antMatchers("/seckillGoods/menus/"
                        , "/seckillGoods/list"
                        , "/seckillGoods/one")//注册请求要放行
                .permitAll()//提交请求
                .anyRequest()//其他请求路径
                .authenticated();//必须鉴权
    }
}
