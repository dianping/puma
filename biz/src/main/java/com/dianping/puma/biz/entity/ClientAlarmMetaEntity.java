package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmMetaEntity extends BaseEntity {

    private String clientName;

    private String emails;

    private boolean alarmByEmail;

    private boolean alarmByWeChat;

    private boolean alarmBySms;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public boolean isAlarmByEmail() {
        return alarmByEmail;
    }

    public void setAlarmByEmail(boolean alarmByEmail) {
        this.alarmByEmail = alarmByEmail;
    }

    public boolean isAlarmByWeChat() {
        return alarmByWeChat;
    }

    public void setAlarmByWeChat(boolean alarmByWeChat) {
        this.alarmByWeChat = alarmByWeChat;
    }

    public boolean isAlarmBySms() {
        return alarmBySms;
    }

    public void setAlarmBySms(boolean alarmBySms) {
        this.alarmBySms = alarmBySms;
    }
}
