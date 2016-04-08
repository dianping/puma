package com.dianping.puma.portal.model;

import com.dianping.puma.portal.constant.PumaClientState;

/**
 * Created by xiaotian.li on 16/4/8.
 * Email: lixiaotian07@gmail.com
 */
public class PumaClientStatus {

    private PumaClientState state;

    private String message;

    public PumaClientState getState() {
        return state;
    }

    public void setState(PumaClientState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
