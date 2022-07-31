package com.offcn.oauth.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.oauth.service.AuthService;
import com.offcn.oauth.util.AuthToken;
import com.offcn.oauth.util.CookieUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/20 16:45
 * @version: 1.0
 */

@RestController
@RequestMapping(value = "/user")
public class AuthController {

    //客户端ID
    @Value("${auth.clientId}")
    private String clientId;

    //秘钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    //Cookie存储的域名
    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    //Cookie生命周期
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public Result login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(username)) {
            throw new RuntimeException("用户名不允许为空");
        }
        if (StringUtils.isEmpty(password)) {
            throw new RuntimeException("密码不允许为空");
        }
        //申请令牌
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);

        //用户身份令牌
        String accessToken = authToken.getAccessToken();
        //将令牌存储到cookie
        CookieUtil.addCookie(response, cookieDomain, "/", "Authorization", accessToken, cookieMaxAge, false);


        return new Result(true, StatusCode.OK, "登录成功！");
    }


}
