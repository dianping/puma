package com.dianping.puma.monitor;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.exception.ConfigException;
import com.dianping.puma.core.exception.MonitorException;
import com.dianping.puma.core.model.container.client.ClientStateContainer;
import com.dianping.puma.core.model.container.storage.StorageStateContainer;
import com.dianping.puma.core.model.state.Storage.StorageState;
import com.dianping.puma.core.model.state.client.ClientState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BinlogFileLaggingMonitor implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(BinlogFileLaggingMonitor.class);

	@Autowired
	StorageStateContainer storageStateContainer;

	@Autowired
	ClientStateContainer clientStateContainer;

	private static final String binlogFileLaggingThresholdKey = "puma.server.binlogFileLaggingThreshold";

	private int binlogFileLaggingThreshold;

	@PostConstruct
	public void init() throws ConfigException {
		try {
			setBinlogFileLaggingThreshold(ConfigCache.getInstance().getIntProperty(binlogFileLaggingThresholdKey));

			ConfigCache.getInstance().addChange(new ConfigChange() {
				@Override
				public void onChange(String key, String value) {
					if (key.equals(binlogFileLaggingThresholdKey)) {
						setBinlogFileLaggingThreshold(Integer.parseInt(value));
					}
				}
			});
		} catch (LionException e) {
			LOG.error("Lion gets values error: {}.", e.getMessage());
			throw new ConfigException(String.format("Lion gets values error: %s.", e.getMessage()));
		}
	}

	@Scheduled(cron = "0/60 * * * * ?")
	public void monitor() throws MonitorException {
		try {
			for (StorageState storageState: storageStateContainer.getAll()) {
				String storageBinlogFile = storageState.getBinlogInfo().getBinlogFile();
				for (ClientState clientState: clientStateContainer.getByTaskName(storageState.getTaskName())) {
					String clientBinlogFile = clientState.getBinlogInfo().getBinlogFile();
					String status = isLagging(clientBinlogFile, storageBinlogFile) ? "1" : Event.SUCCESS;
					Cat.logEvent("Lagging.BinlogFile", clientState.getName(), status, "");
				}
			}
		} catch (Exception e) {
			LOG.warn("Monitor binlog file based lagging error: {}.", e.getMessage());
			throw new MonitorException(e);
		}
	}

	private boolean isLagging(String aBinlogFile, String bBinlogFile) {
		String aBinlogFileNumStr = StringUtils.substringAfterLast(aBinlogFile, ".");
		String bBinlogFileNumStr = StringUtils.substringAfterLast(bBinlogFile, ".");
		long aBinlogFileNum = Long.parseLong(aBinlogFileNumStr);
		long bBinlogFileNum = Long.parseLong(bBinlogFileNumStr);
		return aBinlogFileNum + binlogFileLaggingThreshold <= bBinlogFileNum;
	}

	public int getBinlogFileLaggingThreshold() {
		return binlogFileLaggingThreshold;
	}

	public void setBinlogFileLaggingThreshold(int binlogFileLaggingThreshold) {
		this.binlogFileLaggingThreshold = binlogFileLaggingThreshold;
	}

	@Override
	public void afterPropertiesSet() {}
}
