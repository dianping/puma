package com.dianping.puma.server.service;

import com.dianping.puma.server.model.ClientToken;

/**
 * Created by xiaotian.li on 16/3/31.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientTokenService {

    int update(String clientName, ClientToken clientToken);
}
