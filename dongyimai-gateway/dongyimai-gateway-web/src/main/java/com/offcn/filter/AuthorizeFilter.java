package com.offcn.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //设置令牌头名称
    private static final String AUTHORIZE_TOKEN = "Authorization";

    private static final String USER_LOGIN_URL = "http://localhost:9100/oauth/login";

    /**
     * 全局过滤器
     *
     * @return chain
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求和响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //获取请求的url路径
        String path = request.getURI().getPath();
        //如果是登陆就放行
        if (path.startsWith("/api/user/login") || path.startsWith("/api/brand/search/")
                || path.startsWith("/api/seckillGoods/menus")
                || path.startsWith("/api/seckillGoods/list")
                || path.startsWith("/api/seckillGoods/one")) {
            //放行
            return chain.filter(exchange);
        }
        //获取请求头中令牌头信息
        String authorizeToken = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        //如果为空，则从请求参数中获取
        if (StringUtils.isEmpty(authorizeToken)) {
            authorizeToken = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }
        //如果为空，则从请求cookie中获取
        if (StringUtils.isEmpty(authorizeToken)) {
            //通过获取Cookie的参数，获取jwt
            HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (cookie != null) {
                authorizeToken = cookie.getValue();
                System.out.println(authorizeToken);
            }
        }
        //如果为空，则输出错误代码
        if (StringUtils.isEmpty(authorizeToken)) {
            //response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            //return response.setComplete();
            return needAuthorization(response, USER_LOGIN_URL + "?FROM=http://localhost:8001" + path);
        }
        try {
            //jwt鉴权，成功放行，失败不放行
            //Claims claims = JwtUtil.parseJWT(authorizeToken);
            //request.mutate().header(AUTHORIZE_TOKEN, claims.getSubject());
            //将cookie的令牌存入header中
            if (authorizeToken.startsWith("Bearer ") || authorizeToken.startsWith("bearer ")) {
                request.mutate().header(AUTHORIZE_TOKEN, authorizeToken);
            } else {
                request.mutate().header(AUTHORIZE_TOKEN, "Bearer " + authorizeToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //失败输出鉴权错误
            //response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //return response.setComplete();
            return needAuthorization(response, USER_LOGIN_URL + "?FROM=http://localhost:8001" + path);
        }
        return chain.filter(exchange);
    }

    /**
     * 过滤器执行顺序
     *
     * @return int value
     */
    @Override
    public int getOrder() {
        return 0;
    }

    private Mono<Void> needAuthorization(ServerHttpResponse response, String url) {
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("location", url);
        return response.setComplete();
    }
}