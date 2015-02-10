package com.dianping.puma.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.monitor.ReplicationTaskReportEvent;
import com.dianping.puma.core.replicate.model.task.TaskExecutorStatus;

/*
public enum TaskExecutorStatusContainer {
	instance;
	
	public ReplicationTaskReportEvent getReportEvent(){
		ReplicationTaskReportEvent event = new ReplicationTaskReportEvent();
		Map<String,TaskExecutorStatus> taskExecutorStatus=SystemStatusContainer.instance.listExecutorStatus();
		List<TaskExecutorStatus> taskStatusList=new ArrayList<TaskExecutorStatus>();
		for(Entry<String,TaskExecutorStatus> executorStatus:taskExecutorStatus.entrySet()){
			taskStatusList.add(executorStatus.getValue());
		}
		event.setStatusList(taskStatusList);
		return event;
	}
}*/
