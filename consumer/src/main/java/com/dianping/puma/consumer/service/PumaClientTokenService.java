package com.dianping.puma.consumer.service;

import com.dianping.puma.consumer.model.ClientToken;

/**
 * Created by xiaotian.li on 16/3/31.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientTokenService {

    int update(String clientName, ClientToken clientToken);
}
