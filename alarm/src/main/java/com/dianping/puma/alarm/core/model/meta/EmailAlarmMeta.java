package com.dianping.puma.alarm.core.model.meta;

import lombok.ToString;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class EmailAlarmMeta extends AlarmMeta {

    private List<String> emailRecipients;

    public List<String> getEmailRecipients() {
        return emailRecipients;
    }

    public void setEmailRecipients(List<String> emailRecipients) {
        this.emailRecipients = emailRecipients;
    }
}
