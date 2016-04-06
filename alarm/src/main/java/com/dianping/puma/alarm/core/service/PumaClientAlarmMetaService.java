package com.dianping.puma.alarm.core.service;

import com.dianping.puma.alarm.core.model.meta.EmailAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.LogAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.SmsAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.WeChatAlarmMeta;

import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientAlarmMetaService {

    EmailAlarmMeta findEmail(String clientName);

    WeChatAlarmMeta findWeChat(String clientName);

    SmsAlarmMeta findSms(String clientName);

    LogAlarmMeta findLog(String clientName);

    Map<String, EmailAlarmMeta> findEmailAll();

    Map<String, WeChatAlarmMeta> findWeChatAll();

    Map<String, SmsAlarmMeta> findSmsAll();

    Map<String, LogAlarmMeta> findLogAll();

    void replaceEmail(String clientName, EmailAlarmMeta meta);

    void replaceWeChat(String clientName, WeChatAlarmMeta meta);

    void replaceSms(String clientName, SmsAlarmMeta meta);

    void replaceLog(String clientName, LogAlarmMeta meta);
}
