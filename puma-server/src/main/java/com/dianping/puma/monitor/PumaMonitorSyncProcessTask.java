package com.dianping.puma.monitor;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.common.SystemStatusContainer.ServerStatus;

@Component("pumaMonitorSyncProcessTask")
public class PumaMonitorSyncProcessTask implements PumaMonitorTask {
	private static final Logger LOG = LoggerFactory.getLogger(PumaMonitorSyncProcessTask.class);

	@Override
	public void runTask() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		Map<String, ServerStatus> serverStatuses = SystemStatusContainer.instance.listServerStatus();
		for (Map.Entry<String, ClientStatus> clientStatus : clientStatuses.entrySet()) {
			ServerStatus serverStatus = serverStatuses.get(clientStatus.getValue().getTarget());
			if (clientStatus.getValue().getBinlogFile() == null || serverStatus.getBinlogFile() == null) {
				return;
			}

			Cat.getProducer().logEvent(
					"Puma.server." + clientStatus.getKey() + ".process",
					getEventName(clientStatus.getValue().getBinlogFile(), clientStatus.getValue().getBinlogPos(),
							serverStatus.getBinlogFile(), serverStatus.getBinlogPos()),
					Message.SUCCESS,
					"srcbinlog = " + serverStatus.getBinlogFile() + "," + Long.toString(serverStatus.getBinlogPos())
							+ "&desbinlog = " + serverStatus.getBinlogFile() + ","
							+ Long.toString(serverStatus.getBinlogPos()));
		}
	}

	private String getEventName(String srcFile, long srcPos, String desFile, long desPos) {
		String eventName;
		if (!srcFile.equals(desFile)) {
			String srcStrNum = StringUtils.substring(srcFile, srcFile.length() - 6);
			String desStrNum = StringUtils.substring(desFile, desFile.length() - 6);
			int srcNum = convertStrToInt(srcStrNum);
			int desNum = convertStrToInt(desStrNum);
			int dnum = srcNum - desNum;
			if (dnum == 1 && srcPos < 1000) {
				eventName = "0 file differ";
			} else {
				eventName = Integer.toString(dnum) + " file differ";
			}
		} else {
			eventName = "0 file differ";
		}
		return eventName;
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
			LOG.info("convert string to int exception:" + e);
		}
		return result;
	}

}
