package com.dianping.puma.admin.remote.receiver;

import com.dianping.puma.core.model.state.DumpTaskState;
import com.dianping.puma.biz.event.EventListener;
import com.dianping.puma.biz.event.entity.Event;
import com.dianping.puma.biz.service.DumpTaskService;
import com.dianping.puma.biz.service.DumpTaskStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service("dumpTaskStateReceiver")
public class DumpTaskStateReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(DumpTaskStateReceiver.class);

	@Autowired
	DumpTaskStateService dumpTaskStateService;

	@Autowired
	DumpTaskService dumpTaskService;

	@PostConstruct
	public void init() {
	}

	@Override
	public void onEvent(Event event) {
//		if (event instanceof DumpTaskStateEvent) {
//			LOG.info("Receive dump task state event.");
//
//			List<DumpTaskState> dumpTaskStates = ((DumpTaskStateEvent) event).getTaskStates();
//			for (DumpTaskState dumpTaskState: dumpTaskStates) {
//				dumpTaskStateService.add(dumpTaskState);
//			}
//		}
	}
}
