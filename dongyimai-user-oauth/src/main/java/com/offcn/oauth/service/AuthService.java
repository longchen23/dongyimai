package com.offcn.oauth.service;

import com.offcn.oauth.util.AuthToken;

public interface AuthService {

    /**
     * 授权认证方法
     */
    AuthToken login(String username, String password, String clientId, String clientSecret);

}