package com.dianping.puma.admin.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.dianping.puma.core.sync.DumpConfig;

public class PropertiesConfig {
    private final static PropertiesConfig instance = new PropertiesConfig();
    private List<String> syncServerHosts;
    private Map<Long, DumpConfig.DumpSrc> serverId2mysqlsrc = new HashMap<Long, DumpConfig.DumpSrc>();
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
        String serverId2mysqlsrcStr = p.getProperty("serverId2mysqlsrc");
        if (serverId2mysqlsrcStr != null) {
            String[] serverId2mysqlsrcSplits = serverId2mysqlsrcStr.split(";");
            for (String serverId2mysqlsrc : serverId2mysqlsrcSplits) {
                String[] serverId2mysqlsrcSplits2 = serverId2mysqlsrc.split(",");
                Long serverId = Long.parseLong(serverId2mysqlsrcSplits2[0]);
                String host = serverId2mysqlsrcSplits2[1];
                String username = serverId2mysqlsrcSplits2[2];
                String password = serverId2mysqlsrcSplits2[3];
                DumpConfig.DumpSrc src = new DumpConfig.DumpSrc();
                src.setHost(host);
                src.setUsername(username);
                src.setPassword(password);
                this.serverId2mysqlsrc.put(serverId, src);
            }
        } else {
            throw new IllegalArgumentException("serverId2mysqlsrc must not be null in puma-admin-config.properties.");
        }
        dumpServerHost = p.getProperty("dumpServerHost");
    }

    public DumpConfig.DumpSrc getDumpConfigSrc(Long serverId) {
        return this.serverId2mysqlsrc.get(serverId);
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
