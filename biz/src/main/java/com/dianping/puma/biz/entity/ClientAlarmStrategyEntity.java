package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmStrategyEntity extends BaseEntity {

    private String clientName;

    private boolean noAlarm;

    private boolean linearAlarm;

    private long linearAlarmIntervalInSecond;

    private boolean exponentialAlarm;

    private long minExponentialAlarmIntervalInSecond;

    private long maxExponentialAlarmIntervalInSecond;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public boolean isNoAlarm() {
        return noAlarm;
    }

    public void setNoAlarm(boolean noAlarm) {
        this.noAlarm = noAlarm;
    }

    public boolean isLinearAlarm() {
        return linearAlarm;
    }

    public void setLinearAlarm(boolean linearAlarm) {
        this.linearAlarm = linearAlarm;
    }

    public long getLinearAlarmIntervalInSecond() {
        return linearAlarmIntervalInSecond;
    }

    public void setLinearAlarmIntervalInSecond(long linearAlarmIntervalInSecond) {
        this.linearAlarmIntervalInSecond = linearAlarmIntervalInSecond;
    }

    public boolean isExponentialAlarm() {
        return exponentialAlarm;
    }

    public void setExponentialAlarm(boolean exponentialAlarm) {
        this.exponentialAlarm = exponentialAlarm;
    }

    public long getMinExponentialAlarmIntervalInSecond() {
        return minExponentialAlarmIntervalInSecond;
    }

    public void setMinExponentialAlarmIntervalInSecond(long minExponentialAlarmIntervalInSecond) {
        this.minExponentialAlarmIntervalInSecond = minExponentialAlarmIntervalInSecond;
    }

    public long getMaxExponentialAlarmIntervalInSecond() {
        return maxExponentialAlarmIntervalInSecond;
    }

    public void setMaxExponentialAlarmIntervalInSecond(long maxExponentialAlarmIntervalInSecond) {
        this.maxExponentialAlarmIntervalInSecond = maxExponentialAlarmIntervalInSecond;
    }
}
