package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.syncserver.job.executor.exception.GException;
import com.dianping.puma.syncserver.job.load.Loader;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.transform.Transformer;
import com.dianping.puma.syncserver.job.transform.exception.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncTaskExecutor extends AbstractTaskExecutor<SyncTask, SyncTaskState> {

	protected static final Logger LOG = LoggerFactory.getLogger(SyncTaskExecutor.class);

	private Transformer transformer;

	private Loader loader;

	public SyncTaskExecutor() {}

	/*
	public SyncTaskExecutor(SyncTask syncTask, String pumaServerHost, int pumaServerPort,
			String target, DstDBInstance dstDBInstance) {
		super(syncTask, pumaServerHost, pumaServerPort, target, dstDBInstance);
	}*/

	@Override
	protected void execute(ChangedEvent event) throws GException {
		// Transform.
		TransformException te = transformer.exception();
		if (te != null) {
			handleException(te);
		} else {
			try {
				transformer.transform(event);
			} catch (TransformException e) {
				handleException(e);
			}
		}

		// Load.
		LoadException le = loader.exception();
		if (le != null) {
			handleException(le);
		} else {
			try {
				loader.load(event);
			} catch (LoadException e) {
				handleException(e);
			}
		}
	}

	private void handleException(Exception e) {
		//
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public void setLoader(Loader loader) {
		this.loader = loader;
	}
}
