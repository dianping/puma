package com.dianping.puma.pumaserver.service.impl;

import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.ClientInfoService;
import com.google.common.base.Strings;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 7/3/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultClientInfoService implements ClientInfoService {
    private final Map<String, ClientSession> clients = new ConcurrentHashMap<String, ClientSession>();

    @Override
    public String subscribe(ClientSession client) {
        client.setToken(UUID.randomUUID().toString());
        client.setLastAccessTime(System.currentTimeMillis());
        clients.put(client.getClientName(), client);
        return client.getToken();
    }

    @Override
    public ClientSession get(String clientName, String token) {
        if (Strings.isNullOrEmpty(clientName) || Strings.isNullOrEmpty(token)) {
            return null;
        }

        ClientSession client = clients.get(clientName);
        if (client != null && token.equals(client.getToken())) {
            client.setLastAccessTime(System.currentTimeMillis());
            return client;
        }
        return null;
    }
}
