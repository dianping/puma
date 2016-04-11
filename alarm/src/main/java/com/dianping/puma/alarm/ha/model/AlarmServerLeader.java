package com.dianping.puma.alarm.ha.model;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
@ToString
public class AlarmServerLeader {

    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
