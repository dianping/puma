package com.dianping.puma.admin.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesConfig {
    private final static PropertiesConfig instance = new PropertiesConfig();
    private List<String> syncServerHosts;
    private String dumpServerHost;

    private PropertiesConfig() {
        Properties p = new Properties();
        try {
            p.load(PropertiesConfig.class.getResourceAsStream("/puma-admin-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        String syncServerHostsStr = p.getProperty("syncServerHosts");
        if (syncServerHostsStr != null) {
            String[] hosts = syncServerHostsStr.split(",");
            syncServerHosts = Arrays.asList(hosts);
        } else {
            throw new IllegalArgumentException("pumaServerIps must not be null in puma-admin-config.properties.");
        }
        dumpServerHost = p.getProperty("dumpServerHost");
    }

    public static PropertiesConfig getInstance() {
        return instance;
    }

    public List<String> getSyncServerHosts() {
        return syncServerHosts;
    }

    public String getDumpServerHost() {
        return dumpServerHost;
    }

}
