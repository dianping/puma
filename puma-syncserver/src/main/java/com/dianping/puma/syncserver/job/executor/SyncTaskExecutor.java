package com.dianping.puma.syncserver.job.executor;

import java.sql.SQLException;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.model.state.SyncTaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.executor.exception.DdlRenameException;

public class SyncTaskExecutor extends AbstractTaskExecutor<SyncTask, SyncTaskState> {
	protected static final Logger LOG = LoggerFactory.getLogger(SyncTaskExecutor.class);

	protected SyncTaskState state;

	public SyncTaskExecutor(SyncTask syncTask, String pumaServerHost, int pumaServerPort,
			String target, DstDBInstance dstDBInstance) {
		super(syncTask, pumaServerHost, pumaServerPort, target, dstDBInstance);
	}

	@Override
	protected void execute(ChangedEvent event) throws SQLException, DdlRenameException {
		Transaction t = Cat.getProducer().newTransaction("SQLExecution", this.getTask().getName());
		mysqlExecutor.execute(event);
		t.setStatus("0");
		t.complete();
	}

}
