package com.dianping.puma.server.builder.impl;

import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.server.ReplicationBasedTaskExecutor;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.server.builder.TaskExecutorBuilder;
import org.springframework.stereotype.Service;

@Service("taskExecutorBuilder")
public class DefaultTaskExecutorBuilder implements TaskExecutorBuilder {

	public TaskExecutor build(PumaTask pumaTask) throws Exception {
		return new ReplicationBasedTaskExecutor();
	}
}
