package com.dianping.puma.alarm.core.model.meta;

import lombok.ToString;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class WeChatAlarmMeta extends AlarmMeta {

    private List<String> weChatRecipients;

    public List<String> getWeChatRecipients() {
        return weChatRecipients;
    }

    public void setWeChatRecipients(List<String> weChatRecipients) {
        this.weChatRecipients = weChatRecipients;
    }
}
