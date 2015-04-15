package com.dianping.puma.monitor;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.common.SystemStatusContainer.ServerStatus;
import com.dianping.puma.monitor.exception.MonitorThresholdException;

public class SyncProcessTaskMonitor extends AbstractTaskMonitor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(SyncProcessTaskMonitor.class);

	public static final String SYNCPROCESS_INTERVAL_NAME = "puma.server.interval.syncProcess";
	
	public static final String SYNCPROCESS_DIFF_FILE_NUM ="puma.server.syncProcess.diffNumFile";
	
	private int numThreshold;

	public SyncProcessTaskMonitor(long initialDelay, TimeUnit unit) {
		super(initialDelay, unit);
		LOG.info("SyncProcess Task Monitor started.");
	}

	@Override
	public void doInit(){
		this.setInterval(getLionInterval(SYNCPROCESS_INTERVAL_NAME));
		this.numThreshold = getNumThreshold(SYNCPROCESS_DIFF_FILE_NUM);
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (SYNCPROCESS_INTERVAL_NAME.equals(key)) {
					SyncProcessTaskMonitor.this.setInterval(Long.parseLong(value));
					if(future!=null){
						future.cancel(true);
						if(SyncProcessTaskMonitor.this.executor!=null&&!SyncProcessTaskMonitor.this.executor.isShutdown()
								&&!SyncProcessTaskMonitor.this.executor.isTerminated()){
							SyncProcessTaskMonitor.this.execute(SyncProcessTaskMonitor.this.executor);
						}
					}
				} else if (SYNCPROCESS_DIFF_FILE_NUM.equals(key)) {
					numThreshold =Integer.getInteger(value).intValue();
				}
			}
		});
	}
	
	@Override
	public void doRun() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		Map<String, ServerStatus> serverStatuses = SystemStatusContainer.instance.listServerStatus();
		int dfileNum = 0;
		ClientStatus tempClientStatus = null;
		String tempKey = null;
		for (Map.Entry<String, ClientStatus> clientStatus : clientStatuses.entrySet()) {
			Log.info("puma Monitor SyncProcess : client is " + clientStatus.getKey());
			tempClientStatus = clientStatus.getValue();
			tempKey = clientStatus.getKey();
			ServerStatus serverStatus = serverStatuses.get(clientStatus.getValue().getTarget());
			if (clientStatus.getValue().getBinlogFile() == null || serverStatus.getBinlogFile() == null) {
				continue;
			}
			dfileNum = getDiffNum(serverStatus.getBinlogFile(), serverStatus.getBinlogPos(), clientStatus.getValue()
					.getBinlogFile(), clientStatus.getValue().getBinlogPos());
			Cat.getProducer().logEvent(
					"Puma.server." + clientStatus.getKey() + ".process",
					getEventName(dfileNum),
					Message.SUCCESS,
					"srcbinlog = " + serverStatus.getBinlogFile() + "," + Long.toString(serverStatus.getBinlogPos())
							+ "&desbinlog = " + clientStatus.getValue().getBinlogFile() + ","
							+ Long.toString(clientStatus.getValue().getBinlogPos()));
			try {
				if (dfileNum >= numThreshold) {
					throw new MonitorThresholdException();
				}
			} catch (MonitorThresholdException e) {
				String errorMessage = " diff num of file between sync and server binlog process :  name = " + tempKey
						+ " ip = " + tempClientStatus.getIp() + " target:" + tempClientStatus.getTarget() + " diff = "
						+ Integer.toString(dfileNum);
				// notifyService.alarm(errorMessage, e, true);
				Cat.getProducer().logError(errorMessage, e);
			}
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public Future doExecute(ScheduledExecutorService executor) {
		return executor.scheduleWithFixedDelay(this, initialDelay, interval, unit);
	}

	public void setNumThreshold(int numThreshold) {
		this.numThreshold = numThreshold;
	}

	public int getNumThreshold() {
		return numThreshold;
	}
	
	private String getEventName(int dfileNum) {
		String eventName;
		if (dfileNum == 0) {
			eventName = "0 file differ";
		} else {
			eventName = Integer.toString(dfileNum) + " file differ";
		}
		return eventName;
	}

	private int getDiffNum(String srcFile, long srcPos, String desFile, long desPos) {
		int dfileNum;
		if (!srcFile.equals(desFile)) {
			String srcStrNum = StringUtils.substring(srcFile, srcFile.length() - 6);
			String desStrNum = StringUtils.substring(desFile, desFile.length() - 6);
			int srcNum = convertStrToInt(srcStrNum);
			int desNum = convertStrToInt(desStrNum);
			int dnum = srcNum - desNum;
			if (dnum == 1 && srcPos < 100000) {
				dfileNum = 0;
			} else {
				dfileNum = dnum;
			}
		} else {
			dfileNum = 0;
		}
		return dfileNum;
	}

	private int convertStrToInt(String strNum) {
		int result = 0;
		while (strNum.startsWith("0")) {
			strNum = StringUtils.removeStart(strNum, "0");
		}
		try {
			if (!strNum.isEmpty()) {
				result = Integer.parseInt(strNum);
			}
		} catch (NumberFormatException e) {
			LOG.error("convert string to int exception:" + e);
		}
		return result;
	}
	
	private int getNumThreshold(String keyName) {
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
	

}
