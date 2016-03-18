package com.dianping.puma.alarm.service;

import com.dianping.puma.alarm.model.meta.AlarmMeta;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmMetaService {

    List<AlarmMeta> find(String clientName);
}
