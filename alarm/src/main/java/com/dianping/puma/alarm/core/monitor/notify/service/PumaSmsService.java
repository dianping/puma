package com.dianping.puma.alarm.core.monitor.notify.service;

/**
 * Created by xiaotian.li on 16/3/23.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaSmsService {

    void send(String recipient, String message);
}
