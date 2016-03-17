package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmDataEntity {

    private String clientName;

    private Long pushTimeDelay;

    private Long pullTimeDelay;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getPushTimeDelay() {
        return pushTimeDelay;
    }

    public void setPushTimeDelay(Long pushTimeDelay) {
        this.pushTimeDelay = pushTimeDelay;
    }

    public Long getPullTimeDelay() {
        return pullTimeDelay;
    }

    public void setPullTimeDelay(Long pullTimeDelay) {
        this.pullTimeDelay = pullTimeDelay;
    }
}
