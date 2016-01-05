package com.dianping.puma.instance;

import com.dianping.cat.Cat;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.config.ConfigManager;
import com.dianping.zebra.Constants;
import com.dianping.zebra.group.config.DefaultDataSourceConfigManager;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ZebraInstanceManager implements InstanceManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZebraInstanceManager.class);

    @Autowired
    private ConfigManager configManager;

    private final String env = EnvZooKeeperConfig.getEnv();

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://([^:]+:\\d+)/([^\\?]+).*");

    private ConfigCache configCache = ConfigCache.getInstance();

    private volatile Map<String, Set<SrcDbEntity>> clusterIpMap = new HashMap<String, Set<SrcDbEntity>>();

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
    public Set<SrcDbEntity> getUrlByCluster(String clusterName) {
        return clusterIpMap.get(clusterName.toLowerCase());
    }

    @Override
    public String getClusterByDb(String db) {
        return dbClusterMap.get(db.toLowerCase());
    }

    protected synchronized void buildConfigFromZebra() throws IOException {
        Map<String, Set<SrcDbEntity>> clusterIpMap = new HashMap<String, Set<SrcDbEntity>>();
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
            String writeJdbcUrl = configCache.getProperty(getSingleDataSourceKey(Constants.ELEMENT_JDBC_URL, write.get().getKey()));
            if (Strings.isNullOrEmpty(writeJdbcUrl)) {
                continue;
            }
            Matcher writeMatcher = JDBC_URL_PATTERN.matcher(writeJdbcUrl);
            if (!writeMatcher.matches()) {
                continue;
            }

            String writeUrl = writeMatcher.group(1);
            String db = writeMatcher.group(2).toLowerCase();

            dbClusterMap.put(db, writeUrl);

            Set<SrcDbEntity> clusterIps = clusterIpMap.get(writeUrl);
            if (clusterIps == null) {
                clusterIps = new HashSet<SrcDbEntity>();
                clusterIpMap.put(writeUrl, clusterIps);
            }

            List<String> clusterIpsList = new ArrayList<String>();

            for (Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole> entry : groupdsResult.entrySet()) {
                if (!entry.getValue().isRead() && !entry.getValue().isWrite()) {
                    continue;
                }

                String jdbcUrl = configCache.getProperty(getSingleDataSourceKey(Constants.ELEMENT_JDBC_URL, entry.getKey()));
                String active = configCache.getProperty(getSingleDataSourceKey(Constants.ELEMENT_ACTIVE, entry.getKey()));

                if (Strings.isNullOrEmpty(jdbcUrl) ||
                        Strings.isNullOrEmpty(active) ||
                        !"true".equals(active.toLowerCase())) {
                    continue;
                }
                Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);
                if (!matcher.matches()) {
                    continue;
                }

                String url = matcher.group(1);
                clusterIpsList.add(url);
                SrcDbEntity srcDbEntity = new SrcDbEntity();
                String[] urlAndPort = url.split(":");
                srcDbEntity.setHost(urlAndPort[0]);
                srcDbEntity.setPort(Integer.parseInt(urlAndPort[1]));
                srcDbEntity.setUsername(configCache.getProperty("puma.server.binlog.username"));
                srcDbEntity.setPassword(configCache.getProperty("puma.server.binlog.password"));

                ImmutableSet.Builder<String> tagBuilder = ImmutableSet.builder();
                if (entry.getValue().isRead()) {
                    tagBuilder.add(SrcDbEntity.TAG_READ);
                }
                if (entry.getValue().isWrite()) {
                    tagBuilder.add(SrcDbEntity.TAG_WRITE);
                }

                String tags = configCache.getProperty(getSingleDataSourceKey(Constants.ELEMENT_TAG, entry.getKey()));
                if (!Strings.isNullOrEmpty(tags)) {
                    tagBuilder.addAll(Sets.newHashSet(tags.split(",")));
                }
                srcDbEntity.setTags(tagBuilder.build());

                clusterIps.add(srcDbEntity);
            }

//            LOGGER.info("build cluster info from {} cluster:{}ips: {}", db, writeUrl, ConvertHelper.toJson(clusterIpsList));
        }

        this.clusterIpMap = clusterIpMap;
        this.dbClusterMap = dbClusterMap;
    }

    private String getSingleDataSourceKey(String key, String dsId) {
        return String.format("%s.%s.jdbc.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsId, key);
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}