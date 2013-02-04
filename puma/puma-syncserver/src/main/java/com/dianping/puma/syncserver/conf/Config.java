package com.dianping.puma.syncserver.conf;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.util.IPUtils;
import com.dianping.puma.syncserver.service.PumaSyncServerConfigService;

public class Config implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    @Autowired
    private PumaSyncServerConfigService configService;
    private String syncServerName;
    private static Config instance;

    //    @Value(value = "#{'${puma.dump.tempDir}'}")
    private String tempDir;
    //    @Value(value = "#{'${puma.pumaSyncServer.port}'}")
    private String localPort;

    @PostConstruct
    public void init() {
        //获取本地ip
        for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
            String host = ip + ':' + localPort;
            LOG.info("Try this localhost to find syncServerName from db : " + host);
            PumaSyncServerConfig config = configService.find(host);
            if (config != null) {
                syncServerName = config.getName();
                LOG.info("Match syncServerName: " + syncServerName);
                break;
            } else {
                LOG.info("Not match any syncServerName: " + host);
            }
        }
        if (syncServerName == null) {
            throw new RuntimeException("Cannot try to find the syncServerName, please check the SyncServerConfig in DB.");
        }
        LOG.info("Properties: " + this.toString());

    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public String getSyncServerName() {
        return syncServerName;
    }

    @Override
    public String toString() {
        return "Config [configService=" + configService + ", syncServerName=" + syncServerName + ", tempDir=" + tempDir
                + ", localPort=" + localPort + "]";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public static Config getInstance() {
        return instance;
    }
}
