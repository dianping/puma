package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmDataEntity {

    private String clientName;

    private long pushTimeDelayInSecond;

    private long pullTimeDelayInSecond;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public long getPushTimeDelayInSecond() {
        return pushTimeDelayInSecond;
    }

    public void setPushTimeDelayInSecond(long pushTimeDelayInSecond) {
        this.pushTimeDelayInSecond = pushTimeDelayInSecond;
    }

    public long getPullTimeDelayInSecond() {
        return pullTimeDelayInSecond;
    }

    public void setPullTimeDelayInSecond(long pullTimeDelayInSecond) {
        this.pullTimeDelayInSecond = pullTimeDelayInSecond;
    }
}
