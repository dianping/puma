package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.executor.exception.TEException;
import com.dianping.puma.syncserver.job.load.Loader;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.transform.Transformer;
import com.dianping.puma.syncserver.job.transform.exception.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncTaskExecutor extends AbstractTaskExecutor<SyncTask> {

	protected static final Logger LOG = LoggerFactory.getLogger(SyncTaskExecutor.class);

	private Transformer transformer;

	private Loader loader;

	public SyncTaskExecutor() {}

	@Override
	protected void doStart() {
		loader.start();
		transformer.start();
	}

	@Override
	protected void doStop() {
		transformer.stop();
		loader.stop();
	}

	@Override
	protected void execute(ChangedEvent event) throws TEException {
		try {
			// Transformer.
			transformer.transform(event);

			// Loader.
			loader.asyncThrow();
			loader.load(event);

		} catch (Exception e) {
			throw TEException.translate(e);
		}
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public void setLoader(Loader loader) {
		this.loader = loader;
	}
}
