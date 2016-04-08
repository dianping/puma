package com.dianping.puma.portal.dto;

import com.dianping.puma.portal.constant.PumaServerState;
import lombok.ToString;

/**
 * Created by xiaotian.li on 16/4/6.
 * Email: lixiaotian07@gmail.com
 */
@ToString
public class PumaServerDto {

    private String hostname;

    private String host;

    private double loadAverage;

    private PumaServerState serverStatus;

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

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    public PumaServerState getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(PumaServerState serverStatus) {
        this.serverStatus = serverStatus;
    }
}
