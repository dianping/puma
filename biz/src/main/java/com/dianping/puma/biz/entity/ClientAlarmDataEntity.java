package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmDataEntity {

    private String clientName;

    private Long pushTimestamp;

    private Long pullTimestamp;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getPushTimestamp() {
        return pushTimestamp;
    }

    public void setPushTimestamp(Long pushTimestamp) {
        this.pushTimestamp = pushTimestamp;
    }

    public Long getPullTimestamp() {
        return pullTimestamp;
    }

    public void setPullTimestamp(Long pullTimestamp) {
        this.pullTimestamp = pullTimestamp;
    }
}
