package com.dianping.puma.biz.entity;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/31.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class ClientTokenEntity extends BaseEntity {

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
