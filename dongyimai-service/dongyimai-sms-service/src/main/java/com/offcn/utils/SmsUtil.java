package com.offcn.utils;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SmsUtil {
    @Value("${sms.appcode}")
    private String appcode;
    @Value("${sms.tpl_id}")
    private String tplId;
    private String host = "http://dingxin.market.alicloudapi.com";
    private String path = "/dx/sendSms";
    private String method = "POST";

    public HttpResponse sendSms(String mobile, String code) {
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", mobile);
        querys.put("param", "code:" + code);
        querys.put("tpl_id", tplId);
        Map<String, String> bodys = new HashMap<>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}