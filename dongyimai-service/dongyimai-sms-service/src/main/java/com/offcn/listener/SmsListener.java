package com.offcn.listener;

import com.offcn.config.SimpleQueueConfig;
import com.offcn.utils.SmsUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;

    @RabbitListener(queues = SimpleQueueConfig.SIMPLE_QUEUE)
    public void getMessage(Map<String, String> map) throws Exception {
        if (!CollectionUtils.isEmpty(map)) {
            String mobile = map.get("mobile");
            String code = map.get("code");
            if (!StringUtils.isEmpty(mobile) && !StringUtils.isEmpty(code)) {
                HttpResponse httpResponse = smsUtil.sendSms(mobile, code);
                System.out.println(httpResponse);
            }
        }
    }
}