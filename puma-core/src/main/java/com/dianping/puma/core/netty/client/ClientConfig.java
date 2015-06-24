package com.dianping.puma.core.netty.client;


import com.dianping.puma.core.netty.handler.HandlerFactory;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientConfig {
    private int localPort;

    private int remotePort;

    private String remoteIp;

    private HandlerFactory handlerFactory;

    public HandlerFactory getHandlerFactory() {
        return handlerFactory;
    }

    public ClientConfig setHandlerFactory(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
        return this;
    }

    public int getLocalPort() {
        return localPort;
    }

    public ClientConfig setLocalPort(int localPort) {
        this.localPort = localPort;
        return this;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public ClientConfig setRemotePort(int remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public ClientConfig setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
        return this;
    }
}
