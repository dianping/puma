package com.dianping.puma.biz.service;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmDataService {

    int updatePullTimestamp(String clientName, Long pullTimestamp);

    int updatePushTimestamp(String clientName, Long pushTimestamp);
}
