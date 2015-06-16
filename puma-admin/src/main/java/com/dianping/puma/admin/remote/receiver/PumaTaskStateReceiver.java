package com.dianping.puma.admin.remote.receiver;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.PumaTaskStateEvent;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.PumaTaskStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service("taskStateReceiver")
public class PumaTaskStateReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateReceiver.class);

	@Autowired
	PumaTaskStateService pumaTaskStateService;

	@Autowired
	PumaTaskService pumaTaskService;

	@PostConstruct
	public void init() {
		List<PumaTask> pumaTasks = pumaTaskService.findAll();
		for (PumaTask pumaTask : pumaTasks) {
			if (pumaTask.getPumaServerNames() != null) {
				for (String serverName : pumaTask.getPumaServerNames()) {
					PumaTaskState pumaTaskState = new PumaTaskState();
					pumaTaskState.setName(pumaTaskStateService.getStateName(pumaTask.getName(),serverName));
					pumaTaskState.setTaskName(pumaTask.getName());
					pumaTaskState.setStatus(Status.PREPARING);
					pumaTaskStateService.add(pumaTaskState);
				}
			} else {
				PumaTaskState pumaTaskState = new PumaTaskState();
				pumaTaskState.setName(pumaTaskStateService.getStateName(pumaTask.getName(),pumaTask.getPumaServerName()));
				pumaTaskState.setServerName(pumaTask.getPumaServerName());
				pumaTaskState.setTaskName(pumaTask.getName());
				pumaTaskState.setStatus(Status.PREPARING);
				pumaTaskStateService.add(pumaTaskState);
			}
		}
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof PumaTaskStateEvent) {
			LOG.info("Receive puma task state event.");

			List<PumaTaskState> pumaTaskStates = ((PumaTaskStateEvent) event).getTaskStates();
			for (PumaTaskState pumaTaskState : pumaTaskStates) {
				pumaTaskStateService.add(pumaTaskState);
			}
		}
	}
}
