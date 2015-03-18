package com.dianping.puma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;

@Service("intervalConfig")
public class TaskIntervalConfig {

	private static final Logger LOG = LoggerFactory.getLogger(TaskIntervalConfig.class);
	
	private static final String SEQ_INTERVAL_NAME = "puma.server.interval.seq";
	
	private static final String CLIENTIP_INTERVAL_NAME = "puma.server.interval.ip";
	
	private static final String SERVERINFO_INTERVAL_NAME = "puma.server.interval.serverInfo";
	
	private static final String SYNCPROCESS_INTERVAL_NAME = "puma.server.interval.syncProcess";
	
	private long seqInterval;
	private long clientIpInterval;
	private long serverInfoInterval;
	private long syncProcessInterval;
	
	public TaskIntervalConfig(){
	/*	seqInterval = "0/"+getInterval(SEQ_INTERVAL_NAME)+" * * * * ?";
		clientIpInterval = "0/"+getInterval(CLIENTIP_INTERVAL_NAME)+" * * * * ?";
		serverInfoInterval = "0/"+getInterval(SERVERINFO_INTERVAL_NAME)+" * * * * ?";
	*/
		seqInterval = getInterval(SEQ_INTERVAL_NAME);
		clientIpInterval = getInterval(CLIENTIP_INTERVAL_NAME);
		serverInfoInterval = getInterval(SERVERINFO_INTERVAL_NAME);
		setSyncProcessInterval(getInterval(SYNCPROCESS_INTERVAL_NAME));
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
	
}
