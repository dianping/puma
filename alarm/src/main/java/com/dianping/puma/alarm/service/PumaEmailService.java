package com.dianping.puma.alarm.service;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaEmailService {

    void send(String recipient, String title, String content);
}
