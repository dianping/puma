package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmDataEntity {

    private String clientName;

    private Long pushTimeDelayInSecond;

    private Long pullTimeDelayInSecond;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getPushTimeDelayInSecond() {
        return pushTimeDelayInSecond;
    }

    public void setPushTimeDelayInSecond(Long pushTimeDelayInSecond) {
        this.pushTimeDelayInSecond = pushTimeDelayInSecond;
    }

    public Long getPullTimeDelayInSecond() {
        return pullTimeDelayInSecond;
    }

    public void setPullTimeDelayInSecond(Long pullTimeDelayInSecond) {
        this.pullTimeDelayInSecond = pullTimeDelayInSecond;
    }
}
