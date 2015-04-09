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

@Service("clientLaggingMonitor")
public class ClientLaggingMonitor implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(ClientLaggingMonitor.class);

	@Autowired
	StorageStateContainer storageStateContainer;

	@Autowired
	ClientStateContainer clientStateContainer;

	private static final String CLIENTLAGGING_BINLOGFILE_THRESHOLD_KEY = "puma.server.clientlagging.binlogfile.threshold";

	private static final String CLIENTLAGGING_SEQSPEED_THRESHOLD_KEY = "puma.server.clientlagging.seqspeed.threshold";

	private int clientLaggingBinlogFileThreshold;

	private double clientLaggingSeqSpeedThreshold;

	@PostConstruct
	public void init() throws ConfigException {
		try {
			setClientLaggingBinlogFileThreshold(
					ConfigCache.getInstance().getIntProperty(CLIENTLAGGING_BINLOGFILE_THRESHOLD_KEY));
			setClientLaggingSeqSpeedThreshold(
					ConfigCache.getInstance().getDoubleProperty(CLIENTLAGGING_SEQSPEED_THRESHOLD_KEY));

			ConfigCache.getInstance().addChange(new ConfigChange() {
				@Override
				public void onChange(String key, String value) {
					if (key.equals(CLIENTLAGGING_BINLOGFILE_THRESHOLD_KEY)) {
						setClientLaggingBinlogFileThreshold(Integer.parseInt(value));
					} else if (key.equals(CLIENTLAGGING_SEQSPEED_THRESHOLD_KEY)) {
						setClientLaggingSeqSpeedThreshold(clientLaggingSeqSpeedThreshold);
					}
				}
			});
		} catch (LionException e) {
			LOG.error("Lion gets values error: {}.", e.getMessage());
			throw new ConfigException(String.format("Lion gets values error: %s.", e.getMessage()));
		}
	}

	@Scheduled(cron = "0/60 * * * * ?")
	public void monitor() {
		try {
			for (StorageState storageState : storageStateContainer.getAll()) {
				String storageBinlogFile = storageState.getBinlogInfo().getBinlogFile();
				for (ClientState clientState : clientStateContainer.getByTaskName(storageState.getTaskName())) {
					String clientBinlogFile = clientState.getBinlogInfo().getBinlogFile();
					String status = isBinlogFileLagging(clientBinlogFile, storageBinlogFile) ? "1" : Event.SUCCESS;
					Cat.logEvent("ClientLagging.binlogFile", clientState.getName(), status, "");
				}
			}
		} catch (Exception e) {
			LOG.warn("Monitor client binlog file error: {}.", e.getStackTrace());
		}
	}

	@Scheduled(cron = "0/60 * * * * ?")
	public void clientSeqSpeedMonitor() {
		try {
			for (ClientState clientState : clientStateContainer.getAll()) {
				double clientSeqSpeedPerSecond = clientState.getSeqSpeedPerSecond();
				String status = isSeqSpeedLagging(clientSeqSpeedPerSecond) ? "1" : Event.SUCCESS;
				Cat.logEvent("ClientLagging.seqSpeed", clientState.getName(), status, "");
			}
		} catch (Exception e) {
			LOG.warn("Monitor client seq speed error: {}.", e.getStackTrace());
		}
	}

	private boolean isBinlogFileLagging(String aBinlogFile, String bBinlogFile) {
		String aBinlogFileNumStr = StringUtils.substringAfterLast(aBinlogFile, ".");
		String bBinlogFileNumStr = StringUtils.substringAfterLast(bBinlogFile, ".");
		long aBinlogFileNum = Long.parseLong(aBinlogFileNumStr);
		long bBinlogFileNum = Long.parseLong(bBinlogFileNumStr);
		return aBinlogFileNum + clientLaggingBinlogFileThreshold <= bBinlogFileNum;
	}

	private boolean isSeqSpeedLagging(double speed) {
		return speed < clientLaggingSeqSpeedThreshold;
	}

	public void setClientLaggingBinlogFileThreshold(int clientLaggingBinlogFileThreshold) {
		this.clientLaggingBinlogFileThreshold = clientLaggingBinlogFileThreshold;
	}

	public void setClientLaggingSeqSpeedThreshold(double clientLaggingSeqSpeedThreshold) {
		this.clientLaggingSeqSpeedThreshold = clientLaggingSeqSpeedThreshold;
	}

	@Override
	public void afterPropertiesSet() {
	}
}
