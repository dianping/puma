package com.dianping.puma.pumaserver.service.impl;

import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.exception.binlog.BinlogAuthException;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.status.SystemStatusManager;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger logger = LoggerFactory.getLogger(DefaultClientSessionService.class);

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
                        if (System.currentTimeMillis() - session.getLastAccessTime() > 5 * 60 * 1000
                                && (session.getLastChannel() == null || !session.getLastChannel().isActive())) {
                            needToDestroy.add(session.getClientName());
                        }
                    }

                    for (String name : needToDestroy) {
                        ClientSession session = clients.remove(name);
                        destory(session);

                        logger.info("{}({}) was timeout", session.getClientName(), session.getToken());
                    }
                }
            }
        });
        autoCleanSessionThread.setName("AutoCleanSessionThread");
        autoCleanSessionThread.setDaemon(true);
        autoCleanSessionThread.start();
    }

    @Override
    public String subscribe(ClientSession session) {
        init(session);
        ClientSession old = clients.put(session.getClientName(), session);
        destory(old);
        return session.getToken();
    }

    @Override
    public void unsubscribe(String clientName) {
        if (Strings.isNullOrEmpty(clientName)) {
            return;
        }

        ClientSession client = clients.remove(clientName);
        if (client != null) {
            destory(client);
        }
    }

    protected ClientSession init(ClientSession session) {
        session.setToken(UUID.randomUUID().toString());
        session.setLastAccessTime(System.currentTimeMillis());
        return session;
    }

    protected void destory(ClientSession session) {
        if (session != null) {
            if (session.getAsyncBinlogChannel() != null) {
                session.getAsyncBinlogChannel().destroy();
            }

            if (!clients.containsKey(session.getClientName())) {
                SystemStatusManager.deleteClient(session.getClientName());
            }
        }
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


    @Override
    public ClientSession get(String clientName) {
        if (Strings.isNullOrEmpty(clientName)) {
            return null;
        }
        return clients.get(clientName);
    }
}
