package com.dianping.puma.pumaserver.client;

import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;
import io.netty.channel.Channel;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientSession {

    private final String codec;

    private volatile String token;

    private final String clientName;

    private final AsyncBinlogChannel asyncBinlogChannel;

    private volatile long lastAccessTime;

    private volatile Channel lastChannel;

    public ClientSession(String clientName, AsyncBinlogChannel asyncBinlogChannel, String codec) {
        this.codec = codec;
        this.clientName = clientName;
        this.asyncBinlogChannel = asyncBinlogChannel;
    }

    public String getCodec() {
        return codec;
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

    public Channel getLastChannel() {
        return lastChannel;
    }

    public void setLastChannel(Channel lastChannel) {
        this.lastChannel = lastChannel;
    }
}
