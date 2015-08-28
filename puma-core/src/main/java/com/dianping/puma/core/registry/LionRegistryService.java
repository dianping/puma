package com.dianping.puma.core.registry;

import com.dianping.puma.core.config.ConfigManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LionRegistryService implements RegistryService {

    private static final String ZK_BASE_PATH = "puma-route.server.";

    private static final String ZK_PROJECT = "puma-route";

    private final Logger logger = LoggerFactory.getLogger(LionRegistryService.class);

    @Autowired
    ConfigManager configManager;

    @Override
    public List<String> find(String database) {
        String hostListString = configManager.getConfig(buildKey(database));

        if (hostListString == null) {
            // maybe not created or created but not set.
            try {
                configManager.createConfig(ZK_PROJECT, buildKey(database), database);
            } catch (Throwable t) {
                logger.warn("failed to create config");
            } finally {
                hostListString = "";
            }
        }

        return parseHostList(hostListString);
    }

    @Override
    public void register(String host, String database) {
        List<String> hostList = find(database);

        if (!hostList.contains(host)) {
            hostList.add(host);
            register0(hostList, database);
        }
    }

    @Override
    public void unregister(String host, String database) {
        List<String> hostList = find(database);

        if (hostList.contains(host)) {
            hostList.remove(host);
            register0(hostList, database);
        }
    }

    @Override
    public void registerAll(List<String> hosts, String database) {
        List<String> hostList = find(database);

        boolean needToRegister = false;
        for (String host: hosts) {
            if (!hostList.contains(host)) {
                hostList.add(host);
                needToRegister = true;
            }

            if (needToRegister) {
                register0(hostList, database);
            }
        }
    }

    @Override
    public void unregisterAll(List<String> hosts, String database) {
        List<String> hostList = find(database);

        boolean needToRegister = false;
        for (String host: hosts) {
            if (hostList.contains(host)) {
                hostList.remove(host);
                needToRegister = true;
            }

            if (needToRegister) {
                register0(hostList, database);
            }
        }
    }

    protected void register0(List<String> hostList, String database) {
        String hostListString = buildHostListString(hostList);
        configManager.setConfig(buildKey(database), hostListString);
    }

    protected List<String> parseHostList(String hostListString) {
        return Lists.newArrayList(StringUtils.split(hostListString, "#"));
    }

    protected String buildHostListString(List<String> hostList) {
        return StringUtils.join(hostList, "#");
    }

    protected String buildKey(String database) {
        return ZK_BASE_PATH + database;
    }
}
