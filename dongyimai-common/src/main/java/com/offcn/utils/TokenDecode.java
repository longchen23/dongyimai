package com.offcn.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class TokenDecode {
    //公钥
    private static final String PUBLIC_KEY = "public.key";

    private static String publickey="";


    /**
     * 获取非对称加密公钥 Key
     * @return 公钥 Key
     */
    public String getPubKey() {
        //加载公钥文件
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        //读取输入流
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            //读取第1行，公钥数据
            String s;
            if((s=br.readLine())!=null){
               return s;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }
  
    /***
     * 读取令牌数据
     */
    public Map<String,String> dcodeToken(String token){
        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(getPubKey()));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        return JSON.parseObject(claims,Map.class);
    }
  
    /***
     * 获取用户信息
     * @return
     */
    public Map<String,String> getUserInfo(){
        //获取授权信息
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        //令牌解码
        return dcodeToken(details.getTokenValue());
    }
}