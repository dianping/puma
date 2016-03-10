package com.dianping.puma.common.model;

/**
 * Created by xiaotian.li on 16/3/10.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmData {

    private Long pullTime;

    private Long pushTime;

    public Long getPullTime() {
        return pullTime;
    }

    public void setPullTime(Long pullTime) {
        this.pullTime = pullTime;
    }

    public Long getPushTime() {
        return pushTime;
    }

    public void setPushTime(Long pushTime) {
        this.pushTime = pushTime;
    }
}
