package com.dianping.puma.common.model.message;

/**
 * Created by xiaotian.li on 16/3/12.
 * Email: lixiaotian07@gmail.com
 */
public abstract class EventRequest extends EventMessage {

    private String clientName;

    private String token;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
