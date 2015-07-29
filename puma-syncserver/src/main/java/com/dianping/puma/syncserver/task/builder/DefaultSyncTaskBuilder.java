package com.dianping.puma.syncserver.task.builder;

import com.dianping.puma.biz.entity.SyncTaskEntity;
import com.dianping.puma.syncserver.executor.load.AsyncConcurrentLoader;
import com.dianping.puma.syncserver.executor.load.Loader;
import com.dianping.puma.syncserver.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.executor.TaskExecutor;
import com.dianping.puma.syncserver.transform.DefaultTransformer;
import com.dianping.puma.syncserver.transform.Transformer;
import org.springframework.stereotype.Component;

@Component("syncTaskBuilder")
public class DefaultSyncTaskBuilder implements TaskBuilder<SyncTaskEntity> {

	@Override
	public TaskExecutor build(SyncTaskEntity task) {
		SyncTaskExecutor executor = new SyncTaskExecutor();

		// @todo.
		// Puma client.

		// @todo.
		// duplex buffer.

		// @todo.
		// transformer.
		Transformer transformer = new DefaultTransformer();
		executor.setTransformer(transformer);

		// @todo.
		// loader.
		Loader loader = new AsyncConcurrentLoader();
		executor.setLoader(loader);

		return executor;
	}
}
