package com.dianping.puma.alarm.model.meta;

import lombok.ToString;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
@ToString
public class AlarmMeta {

    private List<String> emails;

    private boolean alarmByEmail;

    private boolean alarmByWeChat;

    private boolean alarmBySms;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
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
