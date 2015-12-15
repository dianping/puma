package com.dianping.puma.api.impl;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaServerMonitor;
import com.dianping.puma.api.cleanup.CleanUp;
import com.dianping.puma.api.cleanup.CleanUpHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ConfigPumaServerMonitor implements PumaServerMonitor {

    private static final String ZK_BASE_PATH = "puma-route.server.";

    protected ConfigCache configManager = ConfigCache.getInstance();

    private final String zkPath;

    private ConfigChange configChangeListener = new ConfigChange() {
        @Override
        public void onChange(String key, String value) {
            if (zkPath.equals(key) && pumaServerMonitorListener != null) {
                pumaServerMonitorListener.onChange(parseServers(value));
            }
        }
    };

    private volatile PumaServerMonitorListener pumaServerMonitorListener;

    public ConfigPumaServerMonitor(String database, List<String> tables) {
        zkPath = buildZkPath(database, tables);
        configManager.addChange(configChangeListener);
        CleanUpHelper.register(this, new LionCleanUpHelper(configManager, configChangeListener));
    }

    @Override
    public List<String> get() {
        String zkNode = configManager.getProperty(zkPath);
        return parseServers(zkNode);
    }

    @Override
    public void addListener(final PumaServerMonitorListener listener) {
        this.pumaServerMonitorListener = listener;
    }

    @Override
    public void removeListener() {
        this.pumaServerMonitorListener = null;
    }

    protected String buildZkPath(String database, List<String> tables) {
        return ZK_BASE_PATH + database;
    }

    protected List<String> parseServers(String zkNode) {
        List<String> servers = new ArrayList<String>();

        if (zkNode == null) {
            return servers;
        }

        String[] serverStrings = StringUtils.split(zkNode, "#");
        if (serverStrings == null) {
            return servers;
        }

        for (int i = 0; i != serverStrings.length; ++i) {
            servers.add(StringUtils.normalizeSpace(serverStrings[i]));
        }
        return servers;
    }

    public static class LionCleanUpHelper implements CleanUp {

        private final ConfigCache configManager;

        private final ConfigChange configChange;

        public LionCleanUpHelper(ConfigCache configManager, ConfigChange configChange) {
            this.configManager = configManager;
            this.configChange = configChange;
        }

        @Override
        public void cleanUp() {
            if (this.configManager != null && this.configChange != null) {
                this.configManager.removeChange(this.configChange);
            }
        }
    }
}
