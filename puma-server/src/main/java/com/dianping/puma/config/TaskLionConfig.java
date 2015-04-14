package com.dianping.puma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;

@Service("taskLionConfig")
public class TaskLionConfig {

	private static final Logger LOG = LoggerFactory.getLogger(TaskLionConfig.class);
	
	private long seqInterval;
	private long clientIpInterval;
	private long serverInfoInterval;
	private long syncProcessInterval;
	private long syncProcessDfileNum;
	public TaskLionConfig(){
		seqInterval = getInterval(ServerLionCommonKey.SEQ_INTERVAL_NAME);
		clientIpInterval = getInterval(ServerLionCommonKey.CLIENTIP_INTERVAL_NAME);
		serverInfoInterval = getInterval(ServerLionCommonKey.SERVERINFO_INTERVAL_NAME);
		syncProcessInterval = getInterval(ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME);
		syncProcessDfileNum = getDiffFileNum(ServerLionCommonKey.SYNCPROCESS_DIFF_FILE_NUM);
	}
	
	protected long getInterval(String intervalName) {
		long interval = 60000;
		try {
			Long temp = ConfigCache.getInstance().getLongProperty(intervalName);
			if (temp != null) {
				interval = temp.longValue();
			}
		} catch (LionException e) {
			LOG.error(e.getMessage(), e);
		}
		return interval;
	}

	private int getDiffFileNum(String keyName){
		int numFile = 2;
		try {
			Integer temp = ConfigCache.getInstance().getIntProperty(keyName);
			if (temp != null) {
				numFile = temp.intValue();
			}
		} catch (LionException e) {
			LOG.error(e.getMessage(), e);
		}
		return numFile;
	}
	
	public void setSeqInterval(long seqInterval) {
		this.seqInterval = seqInterval;
	}

	public long getSeqInterval() {
		return seqInterval;
	}

	public void setClientIpInterval(long clientIpInterval) {
		this.clientIpInterval = clientIpInterval;
	}

	public long getClientIpInterval() {
		return clientIpInterval;
	}

	public void setServerInfoInterval(long serverInfoInterval) {
		this.serverInfoInterval = serverInfoInterval;
	}

	public long getServerInfoInterval() {
		return serverInfoInterval;
	}

	public void setSyncProcessInterval(long syncProcessInterval) {
		this.syncProcessInterval = syncProcessInterval;
	}

	public long getSyncProcessInterval() {
		return syncProcessInterval;
	}

	public void setSyncProcessDfileNum(long syncProcessDfileNum) {
		this.syncProcessDfileNum = syncProcessDfileNum;
	}

	public long getSyncProcessDfileNum() {
		return syncProcessDfileNum;
	}
	
}
