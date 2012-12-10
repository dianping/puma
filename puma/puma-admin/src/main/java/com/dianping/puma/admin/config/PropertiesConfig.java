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
    private List<String> pumaServerIps;
    private Map<Long, DumpConfig.DumpSrc> serverId2mysqlsrc = new HashMap<Long, DumpConfig.DumpSrc>();
    private String pumaSyncServerUrl;

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
        pumaSyncServerUrl = "http://" + p.getProperty("pumaSyncServerIp") + "/puma-syncserver/dump";
    }

    public List<String> getPumaServerIps() {
        return pumaServerIps;
    }

    public DumpConfig.DumpSrc getDumpConfigSrc(Long serverId) {
        return this.serverId2mysqlsrc.get(serverId);
    }

    public static PropertiesConfig getInstance() {
        return instance;
    }

    public String getPumaSyncServerIp() {
        return pumaSyncServerUrl;
    }

}
