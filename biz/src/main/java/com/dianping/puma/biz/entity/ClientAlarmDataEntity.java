package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmDataEntity {

    private String clientName;

    private Long pushTime;

    private Long pullTime;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getPushTime() {
        return pushTime;
    }

    public void setPushTime(Long pushTime) {
        this.pushTime = pushTime;
    }

    public Long getPullTime() {
        return pullTime;
    }

    public void setPullTime(Long pullTime) {
        this.pullTime = pullTime;
    }
}
