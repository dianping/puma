package com.dianping.puma.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ServerStatus;
import com.dianping.puma.config.TaskIntervalConfig;

@Component("pumaMonitorServerInfoTask")
public class PumaMonitorServerInfoTask implements PumaMonitorTask {
 
	private static final Logger LOG = LoggerFactory.getLogger(PumaMonitorServerInfoTask.class);
	
	private Map<String, Long> preUpdateCount;

	private Map<String, Long> preDeleteCount;

	private Map<String, Long> preInsertCount;

	private Map<String, Long> preDdlCount;

	@Autowired
	private TaskIntervalConfig intervalConfig;
	
	public PumaMonitorServerInfoTask() {
		preUpdateCount = new HashMap<String, Long>();
		preDeleteCount = new HashMap<String, Long>();
		preInsertCount = new HashMap<String, Long>();
		preDdlCount = new HashMap<String, Long>();
	}

	@Override
	public void runTask() {
		LOG.info("ServerInfo Cat monitoring....");
		Map<String, ServerStatus> serverStatuses = SystemStatusContainer.instance.listServerStatus();
		Map<String,AtomicLong> insertCount = SystemStatusContainer.instance.listServerRowInsertCounters();
		Map<String,AtomicLong> updateCount = SystemStatusContainer.instance.listServerRowUpdateCounters();
		Map<String,AtomicLong> deleteCount = SystemStatusContainer.instance.listServerRowDeleteCounters();
		Map<String,AtomicLong> ddlCount = SystemStatusContainer.instance.listServerDdlCounters();
		for(Map.Entry<String,ServerStatus> serverStatus:serverStatuses.entrySet()){
			initPreCount(serverStatus.getKey());
			String insertName = " = 0 ";
			String deleteName = " = 0 ";
			String updateName = " = 0 ";
			String ddlName = " = 0 ";
			if(insertCount.containsKey(serverStatus.getKey())){
				insertName = getEventName(preInsertCount.get(serverStatus.getKey()).longValue(),insertCount.get(serverStatus.getKey()).longValue());
				preInsertCount.put(serverStatus.getKey(), insertCount.get(serverStatus.getKey()).longValue());
			}
			if(updateCount.containsKey(serverStatus.getKey())){
				updateName = getEventName(preUpdateCount.get(serverStatus.getKey()).longValue(),updateCount.get(serverStatus.getKey()).longValue());
				preUpdateCount.put(serverStatus.getKey(), updateCount.get(serverStatus.getKey()).longValue());
			}
			if(deleteCount.containsKey(serverStatus.getKey())){
				deleteName = getEventName(preDeleteCount.get(serverStatus.getKey()).longValue(),deleteCount.get(serverStatus.getKey()).longValue());
				preDeleteCount.put(serverStatus.getKey(), deleteCount.get(serverStatus.getKey()).longValue());
			}
			if(ddlCount.containsKey(serverStatus.getKey())){
				ddlName = getEventName(preDdlCount.get(serverStatus.getKey()).longValue(),ddlCount.get(serverStatus.getKey()).longValue());
				preDdlCount.put(serverStatus.getKey(), ddlCount.get(serverStatus.getKey()).longValue());
			}
			Cat.getProducer().logEvent("Puma.server."+serverStatus.getKey()+".insert", insertName, Message.SUCCESS, "name = "+serverStatus.getKey()+"&duration = "+intervalConfig.getServerInfoInterval());
			Cat.getProducer().logEvent("Puma.server."+serverStatus.getKey()+".delete", deleteName, Message.SUCCESS, "name = "+serverStatus.getKey()+"&duration = "+intervalConfig.getServerInfoInterval());
			Cat.getProducer().logEvent("Puma.server."+serverStatus.getKey()+".update", updateName, Message.SUCCESS, "name = "+serverStatus.getKey()+"&duration = "+intervalConfig.getServerInfoInterval());
			Cat.getProducer().logEvent("Puma.server."+serverStatus.getKey()+".ddl", ddlName, Message.SUCCESS, "name = "+serverStatus.getKey()+"&duration = "+intervalConfig.getServerInfoInterval());
		}

	}

	private String getEventName(long preValue,long curValue){
		String eventName;
		long dValue = curValue - preValue;
		if(dValue == 0){
			eventName = " = 0 ";
		}else if(dValue < 2){
			eventName = " < 2 ";
		}else if(dValue < 4){
			eventName = " < 4 ";
		}else if(dValue < 8){
			eventName = " < 8 ";
		}else if(dValue < 16){
			eventName = " < 16 ";
		}else if(dValue < 32){
			eventName = " < 32 ";
		}else if(dValue < 64){
			eventName = " < 64 ";
		}else{
			eventName = " > 64 ";
		}
		return eventName;
	}
	
	private void initPreCount(String key)
	{
		if(!preInsertCount.containsKey(key)){
			preInsertCount.put(key, (long)0);
		}
		if(!preDeleteCount.containsKey(key)){
			preDeleteCount.put(key, (long)0);
		}
		if(!preUpdateCount.containsKey(key)){
			preUpdateCount.put(key, (long)0);
		}
		if(!preDdlCount.containsKey(key)){
			preDdlCount.put(key, (long)0);
		}
	}
}
