package com.dianping.puma.biz.service;

import com.dianping.puma.biz.model.ClientConfig;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientConfigService {

    void create(String clientName, ClientConfig clientConfig);

    int modify(String clientName, ClientConfig clientConfig);

    void replace(String clientName, ClientConfig clientConfig);

    int remove(String clientName);
}
