package com.dianping.puma.datahandler;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.config.PumaServerLionCommonKey;


@Service("tableMetaRefreshFilter")
public class TableMetaRefreshFilter implements InitializingBean{
	
	public static TableMetaRefreshFilter instance;
	
	private static final Logger log = Logger.getLogger(TableMetaRefreshFilter.class);

	private List<String> filtedDatabases;

	//private static final String filtedDatabasesConfigKey = "puma.tablemeta.refresh.filtedDatabases";

	@PostConstruct
	public void start() {
		initFilterDatabases();
	}

	private List<String> constructFilteredDatabasesList(String filtedDatabasesStr) {
		if (StringUtils.isNotBlank(filtedDatabasesStr)) {
			String[] filtedDatabasesArr = StringUtils.split(filtedDatabasesStr, ",");
			if (filtedDatabasesArr != null && filtedDatabasesArr.length > 0) {
				List<String> filtedDatabasesList = new ArrayList<String>(filtedDatabasesArr.length);
				for (String filtedDatabase : filtedDatabasesArr) {
					if (StringUtils.isNotBlank(filtedDatabase)) {
						filtedDatabasesList.add(StringUtils.trim(filtedDatabase).toLowerCase());
					}
				}
				log.info("filted databases:" + filtedDatabasesList);
				return filtedDatabasesList;
			}
		}
		return null;
	}

	public void initFilterDatabases() {
		try {
			String filtedDatabasesStr = ConfigCache.getInstance().getProperty(PumaServerLionCommonKey.FILTERED_DATABASES_CONFIG_KEY);
			filtedDatabases = constructFilteredDatabasesList(filtedDatabasesStr);
			ConfigCache.getInstance().addChange(new ConfigChange() {

				@Override
				public void onChange(String key, String value) {
					if (PumaServerLionCommonKey.FILTERED_DATABASES_CONFIG_KEY.equals(key)) {
						filtedDatabases = constructFilteredDatabasesList(value);
					}
				}
			});
		} catch (LionException e) {
			log.warn(String.format("Get filtedDatabasesConfig[%s] failed.", PumaServerLionCommonKey.FILTERED_DATABASES_CONFIG_KEY));
		}
	}

	public void setFiltedDatabases(List<String> filtedDatabases) {
		this.filtedDatabases = filtedDatabases;
	}

	public List<String> getFiltedDatabases() {
		return filtedDatabases;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}
}
