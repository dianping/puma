package com.dianping.puma.common.service;

import com.dianping.puma.common.model.ClientConfig;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientConfigService {

    int update(String clientName, ClientConfig clientConfig);
}
