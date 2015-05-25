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

import java.util.concurrent.atomic.AtomicLong;

public class SyncTaskExecutor extends AbstractTaskExecutor<SyncTask> {

	protected static final Logger LOG = LoggerFactory.getLogger(SyncTaskExecutor.class);

	private Transformer transformer;

	private Loader loader;

	/** Binlog event delay statistics. */
	private AtomicLong delay = new AtomicLong(0L);

	/** Update statistics. */
	private AtomicLong updates = new AtomicLong(0L);

	/** Insert statistics. */
	private AtomicLong inserts = new AtomicLong(0L);

	/** Delete statistics. */
	private AtomicLong deletes = new AtomicLong(0L);

	/** DDL statistics. */
	private AtomicLong ddls = new AtomicLong(0L);

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

	public AtomicLong getDelay() {
		return delay;
	}

	public AtomicLong getDdls() {
		return ddls;
	}

	public AtomicLong getUpdates() {
		return updates;
	}

	public AtomicLong getInserts() {
		return inserts;
	}

	public AtomicLong getDeletes() {
		return deletes;
	}
}
