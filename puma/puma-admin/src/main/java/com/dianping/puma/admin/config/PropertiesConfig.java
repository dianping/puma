package com.dianping.puma.admin.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesConfig {
    private final static PropertiesConfig instance = new PropertiesConfig();
    private List<String> pumaServerIps;

    private PropertiesConfig() {
        Properties p = new Properties();
        try {
            p.load(PropertiesConfig.class.getResourceAsStream("/puma-admin-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        String pumaServerIpsStr = p.getProperty("pumaServerIps");
        if (pumaServerIpsStr != null) {
            String[] ips = pumaServerIpsStr.split(",");
            pumaServerIps = Arrays.asList(ips);
        } else {
            throw new IllegalArgumentException("pumaServerIps must not be null in puma-admin-config.properties.");
        }
    }

    public List<String> getPumaServerIps() {
        return pumaServerIps;
    }

    public static PropertiesConfig getInstance() {
        return instance;
    }

}
