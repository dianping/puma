package com.dianping.puma.syncserver.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.entity.SyncServerEntity;
import com.dianping.puma.core.service.SyncServerService;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.util.IPUtils;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;
import com.dianping.puma.syncserver.service.PumaSyncServerConfigService;

public class Config implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private static final String SHELL_PATH = "shell/mysqlload.sh";
    @Autowired
    private SyncServerService syncServerService;
    private String syncServerName;
    private static Config instance;

    //    @Value(value = "#{'${puma.dump.tempDir}'}")
    private String tempDir;
    //    @Value(value = "#{'${puma.pumaSyncServer.port}'}")
    private String localPort;

    @PostConstruct
    public void init() throws FileNotFoundException, IOException {
        //获取本地ip
        for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
            String host = ip + ':' + localPort;
            LOG.info("Try this localhost to find syncServerName from db : " + host);
            SyncServerEntity config = syncServerService.findByHost(ip,Integer.parseInt(localPort));
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
        //复制mysqlload.sh到shell目录
        InputStream ins = DumpTaskExecutor.class.getClassLoader().getResourceAsStream(SHELL_PATH);
        String shellFilePath = tempDir + "/" + SHELL_PATH;
        File mysqlLoadShell = new File(shellFilePath);
        FileUtils.touch(mysqlLoadShell);
        IOUtils.copy(ins, new FileOutputStream(mysqlLoadShell));
        LOG.info("created shell : " + shellFilePath);
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
        return "Config [configService=" + syncServerService + ", syncServerName=" + syncServerName + ", tempDir=" + tempDir
                + ", localPort=" + localPort + "]";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public static Config getInstance() {
        return instance;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //复制mysqlload.sh到shell目录
        InputStream ins = Config.class.getClassLoader().getResourceAsStream("shell/mysqlload.sh");
        File mysqlLoadShell = new File("/home/wukezhu/mysqlload.sh");
        IOUtils.copy(ins, new FileOutputStream(mysqlLoadShell));
    }
}
