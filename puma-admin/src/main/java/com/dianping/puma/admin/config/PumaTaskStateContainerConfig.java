package com.dianping.puma.admin.config;

import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.PumaTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class PumaTaskStateContainerConfig {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateContainerConfig.class);

	@Autowired
	TaskStateContainer taskStateContainer;

	@Autowired
	PumaTaskService pumaTaskService;

	@PostConstruct
	public void init() {
		List<PumaTask> pumaTasks = pumaTaskService.findAll();

		for (PumaTask pumaTask: pumaTasks) {
			PumaTaskState taskState = new PumaTaskState();
			taskStateContainer.add(pumaTask.getName(), taskState);
		}
	}
}
