package com.dianping.puma.biz.entity;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmEntity {

    private String clientName;

    private long pullTimeDelayInSecond;

    private long pushTimeDelayInSecond;

    private boolean pullTimeDelay;

    private long minPullTimeDelayInSecond;

    private long maxPullTimeDelayInSecond;

    private boolean pushTimeDelay;

    private long minPushTimeDelayInSecond;

    private long maxPushTimeDelayInSecond;

    private boolean noAlarm;

    private boolean linearAlarm;

    private long linearAlarmIntervalInSecond;

    private boolean exponentialAlarm;

    private long minExponentialIntervalInSecond;

    private long maxExponentialIntervalInSecond;

    private boolean alarmByEmail;

    private List<String> emailRecipients;

    private boolean alarmByWeChat;

    private List<String> weChatRecipients;

    private boolean alarmBySms;

    private List<String> smsRecipients;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public long getPullTimeDelayInSecond() {
        return pullTimeDelayInSecond;
    }

    public void setPullTimeDelayInSecond(long pullTimeDelayInSecond) {
        this.pullTimeDelayInSecond = pullTimeDelayInSecond;
    }

    public long getPushTimeDelayInSecond() {
        return pushTimeDelayInSecond;
    }

    public void setPushTimeDelayInSecond(long pushTimeDelayInSecond) {
        this.pushTimeDelayInSecond = pushTimeDelayInSecond;
    }

    public boolean isPullTimeDelay() {
        return pullTimeDelay;
    }

    public void setPullTimeDelay(boolean pullTimeDelay) {
        this.pullTimeDelay = pullTimeDelay;
    }

    public long getMinPullTimeDelayInSecond() {
        return minPullTimeDelayInSecond;
    }

    public void setMinPullTimeDelayInSecond(long minPullTimeDelayInSecond) {
        this.minPullTimeDelayInSecond = minPullTimeDelayInSecond;
    }

    public long getMaxPullTimeDelayInSecond() {
        return maxPullTimeDelayInSecond;
    }

    public void setMaxPullTimeDelayInSecond(long maxPullTimeDelayInSecond) {
        this.maxPullTimeDelayInSecond = maxPullTimeDelayInSecond;
    }

    public boolean isPushTimeDelay() {
        return pushTimeDelay;
    }

    public void setPushTimeDelay(boolean pushTimeDelay) {
        this.pushTimeDelay = pushTimeDelay;
    }

    public long getMinPushTimeDelayInSecond() {
        return minPushTimeDelayInSecond;
    }

    public void setMinPushTimeDelayInSecond(long minPushTimeDelayInSecond) {
        this.minPushTimeDelayInSecond = minPushTimeDelayInSecond;
    }

    public long getMaxPushTimeDelayInSecond() {
        return maxPushTimeDelayInSecond;
    }

    public void setMaxPushTimeDelayInSecond(long maxPushTimeDelayInSecond) {
        this.maxPushTimeDelayInSecond = maxPushTimeDelayInSecond;
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

    public long getMinExponentialIntervalInSecond() {
        return minExponentialIntervalInSecond;
    }

    public void setMinExponentialIntervalInSecond(long minExponentialIntervalInSecond) {
        this.minExponentialIntervalInSecond = minExponentialIntervalInSecond;
    }

    public long getMaxExponentialIntervalInSecond() {
        return maxExponentialIntervalInSecond;
    }

    public void setMaxExponentialIntervalInSecond(long maxExponentialIntervalInSecond) {
        this.maxExponentialIntervalInSecond = maxExponentialIntervalInSecond;
    }

    public boolean isAlarmByEmail() {
        return alarmByEmail;
    }

    public void setAlarmByEmail(boolean alarmByEmail) {
        this.alarmByEmail = alarmByEmail;
    }

    public List<String> getEmailRecipients() {
        return emailRecipients;
    }

    public void setEmailRecipients(List<String> emailRecipients) {
        this.emailRecipients = emailRecipients;
    }

    public boolean isAlarmByWeChat() {
        return alarmByWeChat;
    }

    public void setAlarmByWeChat(boolean alarmByWeChat) {
        this.alarmByWeChat = alarmByWeChat;
    }

    public List<String> getWeChatRecipients() {
        return weChatRecipients;
    }

    public void setWeChatRecipients(List<String> weChatRecipients) {
        this.weChatRecipients = weChatRecipients;
    }

    public boolean isAlarmBySms() {
        return alarmBySms;
    }

    public void setAlarmBySms(boolean alarmBySms) {
        this.alarmBySms = alarmBySms;
    }

    public List<String> getSmsRecipients() {
        return smsRecipients;
    }

    public void setSmsRecipients(List<String> smsRecipients) {
        this.smsRecipients = smsRecipients;
    }
}
