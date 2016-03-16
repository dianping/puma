package com.dianping.puma.alarm.service;

import com.dianping.puma.common.model.alarm.meta.AlarmMeta;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmMetaService {

    AlarmMeta find(String clientName);
}
