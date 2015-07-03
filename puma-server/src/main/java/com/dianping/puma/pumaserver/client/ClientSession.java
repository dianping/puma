package com.dianping.puma.pumaserver.client;

import com.dianping.puma.pumaserver.channel.BinlogChannel;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientSession {
    private final ClientType clientType;

    private volatile String token;

    private final String clientName;

    private final BinlogChannel binlogChannel;

    private volatile long lastAccessTime;

    public ClientSession(String clientName, BinlogChannel binlogChannel, ClientType clientType) {
        this.clientType = clientType;
        this.clientName = clientName;
        this.binlogChannel = binlogChannel;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public String getToken() {
        return token;
    }

    public String getClientName() {
        return clientName;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public BinlogChannel getBinlogChannel() {
        return binlogChannel;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
