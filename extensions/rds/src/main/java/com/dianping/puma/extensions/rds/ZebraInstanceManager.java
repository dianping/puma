package com.dianping.puma.extensions.rds;

import com.dianping.cat.Cat;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.common.config.ConfigManager;
import com.dianping.puma.portal.db.InstanceManager;
import com.dianping.zebra.Constants;
import com.dianping.zebra.group.config.DefaultDataSourceConfigManager;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ZebraInstanceManager implements InstanceManager {

    @Autowired
    private ConfigManager configManager;

    private final String env = EnvZooKeeperConfig.getEnv();

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://([^:]+:\\d+)/([^\\?]+).*");

    private ConfigCache configCache = ConfigCache.getInstance();

    private volatile Map<String, Set<String>> clusterIpMap = new HashMap<String, Set<String>>();

    private volatile Map<String, String> dbClusterMap = new HashMap<String, String>();

    @Override
    @PostConstruct
    public void init() {
        try {
            buildConfigFromZebra();
        } catch (IOException e) {
            Cat.logError(e.getMessage(), e);
        }

        configCache.addChange(new ConfigChange() {
            @Override
            public void onChange(String key, String value) {
                if (!key.startsWith(Constants.DEFAULT_DATASOURCE_GROUP_PRFIX)
                        && !key.startsWith(Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX)) {
                    return;
                }
                try {
                    buildConfigFromZebra();
                } catch (IOException e) {
                    Cat.logError(e.getMessage(), e);
                }
            }
        });
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void scheduledReload() {
        try {
            buildConfigFromZebra();
        } catch (IOException e) {
            Cat.logError(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getUrlByCluster(String clusterName) {
        return clusterIpMap.get(clusterName.toLowerCase());
    }

    @Override
    public String getClusterByDb(String db) {
        return dbClusterMap.get(db.toLowerCase());
    }

    protected synchronized void buildConfigFromZebra() throws IOException {
        Map<String, Set<String>> clusterIpMap = new HashMap<String, Set<String>>();
        Map<String, String> dbClusterMap = new HashMap<String, String>();

        Map<String, String> allProperties = configManager.getConfigByProject(env, Constants.DEFAULT_DATASOURCE_GROUP_PRFIX);
        for (String groupds : allProperties.values()) {
            Map<String, DefaultDataSourceConfigManager.ReadOrWriteRole> groupdsResult = DefaultDataSourceConfigManager.ReadOrWriteRole.parseConfig(groupds);

            Optional<Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole>> write = FluentIterable.from(groupdsResult.entrySet()).filter(new Predicate<Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole>>() {
                @Override
                public boolean apply(Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole> input) {
                    return input.getValue().isWrite();
                }
            }).first();
            if (!write.isPresent()) {
                continue;
            }
            String writeJdbcUrl = configCache.getProperty(getSingleDataSourceKey("url", write.get().getKey()));
            if (Strings.isNullOrEmpty(writeJdbcUrl)) {
                continue;
            }
            Matcher writeMatcher = JDBC_URL_PATTERN.matcher(writeJdbcUrl);
            if (!writeMatcher.matches()) {
                continue;
            }

            String writeUrl = writeMatcher.group(1);
            String db = writeMatcher.group(2);

            dbClusterMap.put(db, writeUrl);

            Set<String> clusterIps = clusterIpMap.get(writeUrl);
            if (clusterIps == null) {
                clusterIps = new HashSet<String>();
                clusterIpMap.put(writeUrl, clusterIps);
            }

            for (Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole> entry : groupdsResult.entrySet()) {
                if (!entry.getValue().isRead()) {
                    continue;
                }

                String jdbcUrl = configCache.getProperty(getSingleDataSourceKey("url", entry.getKey()));
                if (Strings.isNullOrEmpty(jdbcUrl)) {
                    continue;
                }
                Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);
                if (!matcher.matches()) {
                    continue;
                }

                String url = matcher.group(1);

                clusterIps.add(url);
            }

            this.clusterIpMap = clusterIpMap;
            this.dbClusterMap = dbClusterMap;
        }
    }

    private String getSingleDataSourceKey(String key, String dsId) {
        return String.format("%s.%s.jdbc.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsId, key);
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public Map<String, String> getDbClusterMap() {
        return dbClusterMap;
    }
}