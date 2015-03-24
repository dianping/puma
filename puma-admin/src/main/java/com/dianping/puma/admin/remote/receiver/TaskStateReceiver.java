package com.dianping.puma.admin.remote.receiver;

import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.TaskStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("taskStateReceiver")
public class TaskStateReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(TaskStateReceiver.class);

	@Autowired
	TaskStateContainer taskStateContainer;

	@Override
	public void onEvent(Event event) {
		if (event instanceof TaskStateEvent) {
			List<TaskState> taskStates = ((TaskStateEvent) event).getTaskStates();
			for (TaskState taskState: taskStates) {
				taskStateContainer.add(taskState.getTaskName(), taskState);
			}
		} else {
			LOG.warn("Illegal task state event({}) received.", event);
		}
	}
}
