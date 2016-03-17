package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmBenchmarkEntity extends BaseEntity {

    private String clientName;

    private boolean alarmPullTimeDelay;

    private long maxPullTimeDelayInSecond;

    private boolean alarmPushTimeDelay;

    private long maxPushTimeDelayInSecond;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public boolean isAlarmPullTimeDelay() {
        return alarmPullTimeDelay;
    }

    public void setAlarmPullTimeDelay(boolean alarmPullTimeDelay) {
        this.alarmPullTimeDelay = alarmPullTimeDelay;
    }

    public long getMaxPullTimeDelayInSecond() {
        return maxPullTimeDelayInSecond;
    }

    public void setMaxPullTimeDelayInSecond(long maxPullTimeDelayInSecond) {
        this.maxPullTimeDelayInSecond = maxPullTimeDelayInSecond;
    }

    public boolean isAlarmPushTimeDelay() {
        return alarmPushTimeDelay;
    }

    public void setAlarmPushTimeDelay(boolean alarmPushTimeDelay) {
        this.alarmPushTimeDelay = alarmPushTimeDelay;
    }

    public long getMaxPushTimeDelayInSecond() {
        return maxPushTimeDelayInSecond;
    }

    public void setMaxPushTimeDelayInSecond(long maxPushTimeDelayInSecond) {
        this.maxPushTimeDelayInSecond = maxPushTimeDelayInSecond;
    }
}
