package com.dianping.puma.pumaserver.service;

import com.dianping.puma.pumaserver.client.ClientSession;

/**
 * Dozer @ 7/3/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ClientSessionService {
    /**
     * 订阅并返回一个token
     *
     * @param subscription
     * @return
     */
    String subscribe(ClientSession subscription);

    /**
     * 取消订阅
     *
     * @param clientName
     * @param token
     */
    void unsubscribe(String clientName, String token);

    /**
     * 根据 clientname 和 token 得到 client 信息
     *
     * @param clientName
     * @param token
     * @return
     */
    ClientSession get(String clientName, String token);
}
