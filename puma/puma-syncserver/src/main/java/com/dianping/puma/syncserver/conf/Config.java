package com.dianping.puma.syncserver.conf;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.util.IPUtils;
import com.dianping.puma.syncserver.service.PumaSyncServerConfigService;

public class Config {

    @Autowired
    private PumaSyncServerConfigService configService;

    @Value("#{propertyConfigurer['puma.dump.tempDir']}")
    private String dumpTempDir;
    @Value("#{propertyConfigurer['puma.pumaSyncServer.port']}")
    private String port;
    private String syncServerName;

    public Config() {
        //        Properties p = new Properties();
        //        try {
        //            p.load(Config.class.getResourceAsStream("/puma-syncserver-config.properties"));
        //        } catch (IOException e) {
        //            throw new RuntimeException(e.getMessage(), e);
        //        }
        //        pumaServerHost = p.getProperty("pumaServerHost", "127.0.0.1");
        //        pumaServerPort = Integer.parseInt(p.getProperty("pumaServerPort", "8080"));
        //        dumpTempDir = p.getProperty("dumpTempDir", "/tmp/puma-syncserver");
        //        //获取本地ip
        //        for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
        //
        //        }
    }

    @PostConstruct
    public void init() {
        //获取本地ip
        for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
            String host = ip + ':' + port;
            PumaSyncServerConfig config = configService.find(host);
            if (config != null) {
                syncServerName = config.getName();
            }
        }

    }

    public String getDumpTempDir() {
        return dumpTempDir;
    }

    public void setDumpTempDir(String dumpTempDir) {
        this.dumpTempDir = dumpTempDir;
    }

    public String getSyncServerName() {
        return syncServerName;
    }

    public void setSyncServerName(String syncServerName) {
        this.syncServerName = syncServerName;
    }

}
