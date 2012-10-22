package com.dianping.puma.syncserver.conf;

import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Config instance = new Config();

    public static Config getInstance() {
        return instance;
    }

    /**
     * 重新读取本地config文件
     */
    public static void reload() {
        instance = new Config();
    }

    private String pumaServerHost;
    private int pumaServerPort;

    private Config() {
        Properties p = new Properties();
        try {
            p.load(Config.class.getResourceAsStream("/puma-syncserver-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        pumaServerHost = p.getProperty("pumaServerHost", "127.0.0.1");
        pumaServerPort = Integer.parseInt(p.getProperty("pumaServerPort", "8080"));
    }

    public String getPumaServerHost() {
        return pumaServerHost;
    }

    public void setPumaServerHost(String pumaServerHost) {
        this.pumaServerHost = pumaServerHost;
    }

    public int getPumaServerPort() {
        return pumaServerPort;
    }

    public void setPumaServerPort(int pumaServerPort) {
        this.pumaServerPort = pumaServerPort;
    }

}
