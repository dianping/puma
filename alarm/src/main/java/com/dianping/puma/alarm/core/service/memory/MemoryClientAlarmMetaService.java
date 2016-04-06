package com.dianping.puma.alarm.core.service.memory;

import com.dianping.puma.alarm.core.model.meta.EmailAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.LogAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.SmsAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.WeChatAlarmMeta;
import com.dianping.puma.alarm.core.service.PumaClientAlarmMetaService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryClientAlarmMetaService implements PumaClientAlarmMetaService {

    private ConcurrentMap<String, EmailAlarmMeta> emailAlarmMetaMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, WeChatAlarmMeta> weChatAlarmMetaMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, SmsAlarmMeta> smsAlarmMetaMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, LogAlarmMeta> logAlarmMetaMap
            = new MapMaker().makeMap();

    @Override
    public EmailAlarmMeta findEmail(String clientName) {
        return emailAlarmMetaMap.get(clientName);
    }

    @Override
    public WeChatAlarmMeta findWeChat(String clientName) {
        return weChatAlarmMetaMap.get(clientName);
    }

    @Override
    public SmsAlarmMeta findSms(String clientName) {
        return smsAlarmMetaMap.get(clientName);
    }

    @Override
    public LogAlarmMeta findLog(String clientName) {
        return logAlarmMetaMap.get(clientName);
    }

    @Override
    public Map<String, EmailAlarmMeta> findEmailAll() {
        return ImmutableMap.copyOf(emailAlarmMetaMap);
    }

    @Override
    public Map<String, WeChatAlarmMeta> findWeChatAll() {
        return ImmutableMap.copyOf(weChatAlarmMetaMap);
    }

    @Override
    public Map<String, SmsAlarmMeta> findSmsAll() {
        return ImmutableMap.copyOf(smsAlarmMetaMap);
    }

    @Override
    public Map<String, LogAlarmMeta> findLogAll() {
        return ImmutableMap.copyOf(logAlarmMetaMap);
    }

    @Override
    public void replaceEmail(String clientName, EmailAlarmMeta meta) {
        emailAlarmMetaMap.put(clientName, meta);
    }

    @Override
    public void replaceWeChat(String clientName, WeChatAlarmMeta meta) {
        weChatAlarmMetaMap.put(clientName, meta);
    }

    @Override
    public void replaceSms(String clientName, SmsAlarmMeta meta) {
        smsAlarmMetaMap.put(clientName, meta);
    }

    @Override
    public void replaceLog(String clientName, LogAlarmMeta meta) {
        logAlarmMetaMap.put(clientName, meta);
    }
}
