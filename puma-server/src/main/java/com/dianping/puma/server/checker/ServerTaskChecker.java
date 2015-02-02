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
import com.dianping.puma.core.monitor.ServerTaskActionEvent;
import com.dianping.puma.core.server.model.ServerTaskActionStatus;
import com.dianping.puma.server.Server;
import com.dianping.puma.server.ServerManager;

@Service("serverTaskChecker")
public class ServerTaskChecker implements EventListener {
	public static final Logger LOG = LoggerFactory
			.getLogger(ServerTaskChecker.class);

	@Autowired
	private NotifyService notifyService;

	@Autowired
	private ServerManager serverManager;

	@PostConstruct
	public void init() {
		ConcurrentHashMap<Long, Server> configedServers = null;

		try {
			configedServers = serverManager.constructServers();
		} catch (Exception e) {
			LOG.error("constructed servers failed....");
			e.printStackTrace();
			return;
		}
		if (configedServers != null) {
			LOG.info("Starting " + configedServers.size()
					+ " servers configured.");
			// start servers
			for (Map.Entry<Long, Server> item : configedServers.entrySet()) {
				serverManager.initContext(item.getValue());
				// serverManager.start(item.getValue());
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
		if (event instanceof ServerTaskActionEvent) {
			ServerTaskActionEvent taskActionEvent = (ServerTaskActionEvent) event;
			ServerTaskActionStatus action = taskActionEvent
					.getTaskStatusAction();
			switch (action) {
			case START:
				serverManager.startEvent(taskActionEvent);
				break;
			case STOP:
				serverManager.stopEvent(taskActionEvent);
				break;
			case RESTART:
				serverManager.restartEvent(taskActionEvent);
			case ADD:
				serverManager.addEvent(taskActionEvent);
			case DELETE:
				serverManager.deleteEvent(taskActionEvent);
			case UPDATE:
				serverManager.updateEvent(taskActionEvent);
			}
		}
	}

}
