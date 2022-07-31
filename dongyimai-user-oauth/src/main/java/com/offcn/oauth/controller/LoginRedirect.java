package com.offcn.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Controller
@RequestMapping(value = "/oauth")
public class LoginRedirect {

    /**
     * 跳转到登录页
     *
     * @param from 如果是直接进入登录页,该from参数可以不需要,有默认值为''
     *             如果是访问资源页,该from参数的值由网关提供，值是该资源页的路径
     * @return
     */
    @GetMapping(value = "/login")
    public String login(@RequestParam(name = "FROM", required = false, defaultValue = "") String from, Model model) {
        //由于url中的字符编码非utf-8，需要需要字符编码转换
        try {
            String decodeUrl = URLDecoder.decode(from, "UTF-8");
            //将资源页路径，返显到登录页地址栏
            model.addAttribute("from", decodeUrl);
            System.out.println("from:" + decodeUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "login";
    }
}