package com.dianping.puma.pumaserver.client;

import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientSession {
    private final ClientType clientType;

    private volatile String token;

    private final String clientName;

    private final AsyncBinlogChannel asyncBinlogChannel;

    private volatile long lastAccessTime;

    public ClientSession(String clientName, AsyncBinlogChannel asyncBinlogChannel, ClientType clientType) {
        this.clientType = clientType;
        this.clientName = clientName;
        this.asyncBinlogChannel = asyncBinlogChannel;
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

    public AsyncBinlogChannel getAsyncBinlogChannel() {
        return asyncBinlogChannel;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
