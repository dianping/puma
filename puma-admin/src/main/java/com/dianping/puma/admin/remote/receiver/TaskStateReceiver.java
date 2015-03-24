package com.dianping.puma.admin.remote.receiver;

import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("taskStateReceiver")
public class TaskStateReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(TaskStateReceiver.class);

	@Autowired
	TaskStateContainer taskStateContainer;

	@Override
	public void onEvent(Event event) {
	}
}
