package com.dianping.puma.checker;

import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.model.PumaTaskOperation;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.server.Server;
import com.dianping.puma.server.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("pumaTaskChecker")
public class PumaTaskChecker implements EventListener {

	public static final Logger LOG = LoggerFactory.getLogger(PumaTaskChecker.class);

	@Autowired
	private TaskManager taskManager;

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
		LOG.info("Receive event.");

		if (event instanceof PumaTaskOperationEvent) {
			LOG.info("Receive `PumaTaskOperationEvent`.");

			try {
				PumaTaskOperationEvent pumaTaskOperationEvent = (PumaTaskOperationEvent) event;
				PumaTaskOperation pumaTaskOperation = pumaTaskOperationEvent.getOperation();
				Operation operation = pumaTaskOperation.getOperation();

				// @TODO
				switch (operation) {
				case CREATE:
					taskManager.createEvent(pumaTaskOperationEvent);
					break;
				case UPDATE:
					taskManager.updateEvent(pumaTaskOperationEvent);
					break;
				case REMOVE:
					taskManager.removeEvent(pumaTaskOperationEvent);
					break;
				}
			} catch (Exception e) {
				LOG.error("Receive `PumaTaskOperationEvent` error: %s.", e.getMessage());
			}
		}
	}
}
