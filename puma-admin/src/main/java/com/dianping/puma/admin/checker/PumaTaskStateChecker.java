package com.dianping.puma.admin.checker;

import com.dianping.puma.core.container.PumaTaskStateContainer;
import com.dianping.puma.core.model.PumaTaskState;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.PumaTaskStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaTaskStateChecker")
public class PumaTaskStateChecker implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateChecker.class);

	@Autowired
	PumaTaskStateContainer pumaTaskStateContainer;

	@Override
	public void onEvent(Event event) {
		LOG.info("Receive puma task state event.");

		try {
			PumaTaskStateEvent pumaTaskStateEvent = (PumaTaskStateEvent) event;
			List<String> taskIds = pumaTaskStateEvent.getTaskIds();
			List<PumaTaskState> states = pumaTaskStateEvent.getStates();

			for (int i = 0; i != taskIds.size() && i != states.size(); ++i) {
				pumaTaskStateContainer.update(taskIds.get(i), states.get(i));
			}
		} catch (Exception e) {
			LOG.error("Receive puma task state event error: {}.", e.getMessage());
		}
	}
}
