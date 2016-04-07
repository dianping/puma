package com.dianping.puma.portal.device;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/4/7.
 * Email: lixiaotian07@gmail.com
 */
@ToString
public class PumaDevice {

    private String hostname;

    private String host;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
