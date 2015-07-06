package com.dianping.puma.pumaserver.service.impl;

import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.exception.binlog.BinlogAuthException;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 7/3/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultClientSessionService implements ClientSessionService {
    private final Map<String, ClientSession> clients = new ConcurrentHashMap<String, ClientSession>();

    private Thread autoCleanSessionThread;

    public synchronized void init() {
        autoCleanSessionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    List<String> needToDestroy = new ArrayList<String>();
                    for (ClientSession session : clients.values()) {
                        if (System.currentTimeMillis() - session.getLastAccessTime() > 60 * 60 * 1000) {
                            needToDestroy.add(session.getClientName());
                        }
                    }

                    for (String name : needToDestroy) {
                        ClientSession session = clients.remove(name);
                        if (session == null) {
                            continue;
                        }
                        session.getBinlogChannel().destroy();
                    }
                }
            }
        });
        autoCleanSessionThread.setName("AutoCleanSessionThread");
        autoCleanSessionThread.setDaemon(true);
        autoCleanSessionThread.start();
    }

    @Override
    public String subscribe(ClientSession client) {
        client.setToken(UUID.randomUUID().toString());
        client.setLastAccessTime(System.currentTimeMillis());
        ClientSession old = clients.put(client.getClientName(), client);
        if (old != null) {
            old.getBinlogChannel().destroy();
        }
        return client.getToken();
    }

    @Override
    public void unsubscribe(String clientName, String token) {
        if (Strings.isNullOrEmpty(clientName) || Strings.isNullOrEmpty(token)) {
            return;
        }

        ClientSession client = clients.get(clientName);
        if (client != null && token.equals(client.getToken())) {
            unsubscribe(client);
        }
    }

    protected void unsubscribe(ClientSession session) {
        clients.remove(session.getClientName());
        session.getBinlogChannel().destroy();
    }

    @Override
    public ClientSession get(String clientName, String token) {
        if (Strings.isNullOrEmpty(clientName) || Strings.isNullOrEmpty(token)) {
            throw new BinlogAuthException(clientName);
        }

        ClientSession client = clients.get(clientName);
        if (client != null && token.equals(client.getToken())) {
            client.setLastAccessTime(System.currentTimeMillis());
            return client;
        }

        throw new BinlogAuthException(clientName);
    }
}
