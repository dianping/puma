package com.dianping.puma.consumer.service;

import com.dianping.puma.consumer.model.ClientConfig;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientConfigService {

    int update(String clientName, ClientConfig clientConfig);
}
