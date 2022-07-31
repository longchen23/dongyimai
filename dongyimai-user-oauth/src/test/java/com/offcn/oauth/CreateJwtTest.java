package com.offcn.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;

public class CreateJwtTest {

    @Test
    public void testCreateToken() {
        //证书文件路径
        String keyLocation = "dongyimai.jks";
        //密钥库密码
        String keyPassword = "dongyimai";
        //密钥密码
        String pwd = "dongyimai";
        //密钥别名
        String alias = "dongyimai";

        //访问证书路径
        ClassPathResource resource = new ClassPathResource(keyLocation);
        //创建密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, keyPassword.toCharArray());
        //读取密钥对（公钥，私钥）
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, pwd.toCharArray());
        //获取私钥
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        //定义payload
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("id", "1");
        payload.put("name", "longchen");
        payload.put("roles", "admin,user");
        //生成Jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(rsaPrivateKey));
        //取出令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }


}
