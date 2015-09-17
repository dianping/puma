package com.dianping.puma.pumaserver.client;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public enum ClientType {
    UNKNOW(0), PUMACLIENT(1), BROSWER(2);

    private final int type;

    ClientType(int type) {
        this.type = type;
    }
    
    public int getType(){
   	 return type;
    }
}
