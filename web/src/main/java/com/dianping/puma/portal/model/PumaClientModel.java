package com.dianping.puma.portal.model;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaClientModel {
    private String clientName;
    private long beginTime;

    public String getClientName() {
        return clientName;
    }

    public PumaClientModel setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public PumaClientModel setBeginTime(long beginTime) {
        this.beginTime = beginTime;
        return this;
    }
}
