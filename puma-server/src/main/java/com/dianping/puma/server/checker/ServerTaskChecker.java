package com.dianping.puma.server.checker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.model.replication.ReplicationTaskStatus;
import com.dianping.puma.monitor.ReplicationTaskStatusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusActionEvent;
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

	@Autowired
	private ReplicationTaskStatusContainer replicationTaskStatusContainer;

	@PostConstruct
	public void init() {
		ConcurrentHashMap<String, Server> configedServers = null;

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
			for (Map.Entry<String, Server> item : configedServers.entrySet()) {
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
		if (event instanceof ReplicationTaskStatusActionEvent) {
			ReplicationTaskStatusActionEvent taskStatusEvent = (ReplicationTaskStatusActionEvent) event;
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
				break;
			}
		} else if (event instanceof ReplicationTaskEvent) {
			ReplicationTaskEvent taskEvent = (ReplicationTaskEvent) event;
			ActionType action = taskEvent.getActionType();
			switch (action) {
			case ADD:
				taskManager.addEvent(taskEvent);
				break;
			case DELETE:
				taskManager.deleteEvent(taskEvent);
				break;
			case UPDATE:
				taskManager.updateEvent(taskEvent);
				break;
			}
		}

	}

}
