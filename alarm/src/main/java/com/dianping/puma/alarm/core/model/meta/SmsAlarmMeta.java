package com.dianping.puma.alarm.core.model.meta;

import lombok.ToString;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class SmsAlarmMeta extends AlarmMeta {

    private List<String> smsRecipients;

    public List<String> getSmsRecipients() {
        return smsRecipients;
    }

    public void setSmsRecipients(List<String> smsRecipients) {
        this.smsRecipients = smsRecipients;
    }
}
