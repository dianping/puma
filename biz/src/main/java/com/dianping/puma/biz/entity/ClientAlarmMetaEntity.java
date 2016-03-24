package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmMetaEntity extends BaseEntity {

    private String clientName;

    private boolean alarmByLog;

    private boolean alarmByEmail;

    private String emailRecipients;

    private boolean alarmByWeChat;

    private String weChatRecipients;

    private boolean alarmBySms;

    private String smsRecipients;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public boolean isAlarmByLog() {
        return alarmByLog;
    }

    public void setAlarmByLog(boolean alarmByLog) {
        this.alarmByLog = alarmByLog;
    }

    public boolean isAlarmByEmail() {
        return alarmByEmail;
    }

    public void setAlarmByEmail(boolean alarmByEmail) {
        this.alarmByEmail = alarmByEmail;
    }

    public String getEmailRecipients() {
        return emailRecipients;
    }

    public void setEmailRecipients(String emailRecipients) {
        this.emailRecipients = emailRecipients;
    }

    public boolean isAlarmByWeChat() {
        return alarmByWeChat;
    }

    public void setAlarmByWeChat(boolean alarmByWeChat) {
        this.alarmByWeChat = alarmByWeChat;
    }

    public String getWeChatRecipients() {
        return weChatRecipients;
    }

    public void setWeChatRecipients(String weChatRecipients) {
        this.weChatRecipients = weChatRecipients;
    }

    public boolean isAlarmBySms() {
        return alarmBySms;
    }

    public void setAlarmBySms(boolean alarmBySms) {
        this.alarmBySms = alarmBySms;
    }

    public String getSmsRecipients() {
        return smsRecipients;
    }

    public void setSmsRecipients(String smsRecipients) {
        this.smsRecipients = smsRecipients;
    }
}
