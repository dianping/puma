package com.dianping.puma.alarm.service;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class EmailAlarmDebug {

    public static void main(String[] args) {
        EmailAlarmService emailAlarm = new EmailAlarmService();
        emailAlarm.setHttpPath("http://web.paas.dp/mail/send");

        HttpClient httpClient = HttpClients.createDefault();
        emailAlarm.setHttpClient(httpClient);

        emailAlarm.start();
        emailAlarm.alarm("xiaotian.li", "test_head", "test_content");
    }
}