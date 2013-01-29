package com.dianping.puma.syncserver.conf;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.util.IPUtils;
import com.dianping.puma.syncserver.service.PumaSyncServerConfigService;

@Service
public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    @Autowired
    private PumaSyncServerConfigService configService;

    @Value(value = "#{'${puma.dump.tempDir}'}")
    private String dumpTempDir;
    @Value(value = "#{'${puma.pumaSyncServer.port}'}")
    private String localPort;
    private String syncServerName;

    @PostConstruct
    public void init() {
        //获取本地ip
        for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
            String host = ip + ':' + localPort;
            LOG.info("Try this ip to find syncServerName from db : " + ip);
            PumaSyncServerConfig config = configService.find(host);
            if (config != null) {
                syncServerName = config.getName();
                LOG.info("Match syncServerName: " + syncServerName);
                break;
            } else {
                LOG.info("Not match any syncServerName: " + ip);
            }
        }
        if (syncServerName == null) {
            throw new RuntimeException("Cannot try to find the syncServerName, please check the SyncServerConfig in DB.");
        }
        LOG.info("Properties: " + this.toString());

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

    @Override
    public String toString() {
        return "Config [dumpTempDir=" + dumpTempDir + ", localPort=" + localPort + ", syncServerName=" + syncServerName + "]";
    }

}
