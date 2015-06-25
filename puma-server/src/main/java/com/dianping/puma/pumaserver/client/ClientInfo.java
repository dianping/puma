package com.dianping.puma.pumaserver.client;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientInfo {
    private ClientType clientType;

    public ClientType getClientType() {
        return clientType;
    }

    public ClientInfo setClientType(ClientType clientType) {
        this.clientType = clientType;
        return this;
    }
}
