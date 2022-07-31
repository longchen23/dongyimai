package com.offcn.oauth.service.impl;

import com.offcn.oauth.service.AuthService;
import com.offcn.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/20 16:10
 * @version: 1.0
 */

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        return applyToken(username, password, clientId, clientSecret);

    }

    /**
     * 认证方法
     *
     * @param username:用户登录名字
     * @param password：用户密码
     * @param clientId：配置文件中的客户端ID
     * @param clientSecret：配置文件中的秘钥
     * @return username, password, clientId, clientSecret
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //选中需要认证的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        System.out.println("serviceInstance:"+serviceInstance);
        //如果地址为空，抛出异常
        if (serviceInstance == null) {
            throw new RuntimeException("找不到对应服务");
        }
        //获取令牌中的uri
        String path = serviceInstance.getUri().toString() + "/oauth/token";
        //定义body信息
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //授权方式
        body.add("grant_type", "password");
        //账号
        body.add("username", username);
        //密码
        body.add("password", password);
        //定义头信息
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", httpbasic(clientId, clientSecret));
        //指定restTemplate遇到400或401响应码时候也不要抛出异常
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        Map map = null;
        try {
            //http请求spring security的申请令牌接口
            System.out.println("path:"+path);
            ResponseEntity<Map> exchange = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, header), Map.class);
            map = exchange.getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //访问令牌
        assert map != null;
        String accessToken = (String) map.get("access_token");
        //刷新令牌
        String refreshToken = (String) map.get("refresh_token");
        //jti用户身份标识
        String jti = (String) map.get("jti");
        if (CollectionUtils.isEmpty(map) || StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken) || StringUtils.isEmpty(jti)) {
            throw new RuntimeException("创建令牌失败");
        }
        //将响应数据封装成AuthToken对象
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(accessToken);
        authToken.setRefreshToken(refreshToken);
        authToken.setJti(jti);
        return authToken;
    }


    /**
     * base64编码
     *
     * @param clientId
     * @param clientSecret
     * @return clientId, clientSecret
     */
    private String httpbasic(String clientId, String clientSecret) {
        //按照clientId+clientSecret拼接成客户端id：密码形式
        String value = clientId + ":" + clientSecret;
        //进行base64编码转成字节数组
        byte[] encode = Base64Utils.encode(value.getBytes());
        //再返回字符串
        return "Basic " + new String(encode);

    }
}
