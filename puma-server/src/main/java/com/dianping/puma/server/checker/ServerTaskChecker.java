package com.dianping.puma.server.checker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusEvent;
import com.dianping.puma.core.replicate.model.task.ActionType;
import com.dianping.puma.core.replicate.model.task.StatusActionType;
import com.dianping.puma.server.Server;
import com.dianping.puma.server.TaskManager;

@Service("serverTaskChecker")
public class ServerTaskChecker implements EventListener {
	public static final Logger LOG = LoggerFactory
			.getLogger(ServerTaskChecker.class);

	@Autowired
	private NotifyService notifyService;

	@Autowired
	private TaskManager taskManager;

	@PostConstruct
	public void init() {
		ConcurrentHashMap<Long, Server> configedServers = null;

		try {
			configedServers = taskManager.constructServers();
		} catch (Exception e) {
			LOG.error("constructed servers failed....");
            throw new RuntimeException("Cannot try to constructServers , please check the ReplicationTask in DB.");
		}
		if (configedServers != null) {
			LOG.info("Starting " + configedServers.size()
					+ " servers configured.");
			// start servers
			for (Map.Entry<Long, Server> item : configedServers.entrySet()) {
				taskManager.initContext(item.getValue());
				taskManager.startServer(item.getValue());
				LOG.info("Server " + item.getValue().getServerName()
						+ " started at binlogFile: "
						+ item.getValue().getContext().getBinlogFileName()
						+ " position: "
						+ item.getValue().getContext().getBinlogStartPos());
			}
		}
	}

	@Override
	public void onEvent(Event event) {
		LOG.info("Receive event: " + event);
		if (event instanceof ReplicationTaskStatusEvent) {
			ReplicationTaskStatusEvent taskStatusEvent = (ReplicationTaskStatusEvent) event;
			StatusActionType action = taskStatusEvent.getStatusActionType();
			switch (action) {
			case START:
				taskManager.startEvent(taskStatusEvent);
				break;
			case STOP:
				taskManager.stopEvent(taskStatusEvent);
				break;
			case RESTART:
				taskManager.restartEvent(taskStatusEvent);
			}
		} else if (event instanceof ReplicationTaskEvent) {
			ReplicationTaskEvent taskEvent = (ReplicationTaskEvent) event;
			ActionType action = taskEvent.getActionType();
			switch (action) {
			case ADD:
				taskManager.addEvent(taskEvent);
			case DELETE:
				taskManager.deleteEvent(taskEvent);
			case UPDATE:
				taskManager.updateEvent(taskEvent);
			}
		}

	}

}
