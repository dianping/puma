package com.dianping.puma.instance;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.biz.dao.PumaTaskTargetDao;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.zebra.Constants;
import com.dianping.zebra.biz.service.LionService;
import com.dianping.zebra.group.config.DefaultDataSourceConfigManager;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ZebraInstanceManager extends AbstractInstanceManager {

	@Autowired
	private LionService lionService;

	@Autowired
	private PumaTaskTargetDao pumaTaskTargetDao;

	@Autowired
	private PumaServerService pumaServerService;

	private final String env = EnvZooKeeperConfig.getEnv();

	private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://([^:]+:\\d+)/([^\\?]+).*");

	private ConfigCache configCache = ConfigCache.getInstance();

	private volatile Map<String, Set<String>> clusterIpMap = new HashMap<String, Set<String>>();

	@SuppressWarnings("unused")
   private volatile Map<String, Set<String>> clusterDbMap = new HashMap<String, Set<String>>();

	@Override
	@PostConstruct
	public void init() {
		try {
			buildConfigFromZebra();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getUrlByCluster(String clusterName) {
		return clusterIpMap.get(clusterName);
	}

	protected void buildConfigFromZebra() throws IOException {
		// Map<String, Set<String>> targets = getTargets();
		// Map<String, InstanceChangedEvent> cachedEvent = new HashMap<String, InstanceChangedEvent>();

		Map<String, Set<String>> clusterIpMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> clusterDbMap = new HashMap<String, Set<String>>();

		Map<String, String> allProperties = lionService.getConfigByProject(env, Constants.DEFAULT_DATASOURCE_GROUP_PRFIX);
		for (String groupds : allProperties.values()) {
			Map<String, DefaultDataSourceConfigManager.ReadOrWriteRole> groupdsResult = DefaultDataSourceConfigManager.ReadOrWriteRole
			      .parseConfig(groupds);

			Optional<Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole>> write = FluentIterable
			      .from(groupdsResult.entrySet())
			      .filter(new Predicate<Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole>>() {
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
			String db = writeMatcher.group(2).toLowerCase();

			Set<String> clusterDbs = clusterDbMap.get(writeUrl);
			if (clusterDbs == null) {
				clusterDbs = new HashSet<String>();
				clusterDbMap.put(writeUrl, clusterDbs);
			}
			clusterDbs.add(db);

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

			this.clusterDbMap = clusterDbMap;
			this.clusterIpMap = clusterIpMap;
		}
	}

	private String getSingleDataSourceKey(String key, String dsId) {
		return String.format("%s.%s.jdbc.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsId, key);
	}

	public void setLionService(LionService lionService) {
		this.lionService = lionService;
	}

	// protected Map<String, Set<String>> getTargets() {
	// List<PumaServerEntity> servers = pumaServerService.findOnCurrentServer();
	// Map<String, Set<String>> targetResult = new HashMap<String, Set<String>>();
	// for (PumaServerEntity server : servers) {
	// List<PumaTaskTargetEntity> targets = pumaTaskTargetDao.findByTaskId(server.getId());
	// for (PumaTaskTargetEntity target : targets) {
	// Set<String> tables = targetResult.get(target.getDatabase());
	// if (tables == null) {
	// tables = new HashSet<String>();
	// targetResult.put(target.getDatabase(), tables);
	// }
	// tables.add(target.getTable());
	// }
	// }
	// return targetResult;
	// }
}