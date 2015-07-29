package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.exception.PumaException;
import com.dianping.puma.syncserver.executor.load.condition.ConditionChain;
import com.dianping.puma.syncserver.util.SqlParser;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.*;

public class AsyncConcurrentLoader extends AbstractLoader {

	// Injected.
	protected DataSource dataSource;

	// Injected.
	protected ExecutorService executorService;

	// Injected.
	protected ConditionChain conditionChain;

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public void load(ChangedEvent binlogEvent, LoadCallback loadCallback) {
		if (checkStop()) {
			throw new PumaException("load binlog event failure, load module stopped.");
		}

		Callable<Integer> loadCallable = genLoadTask(binlogEvent);
		ListenableFutureTask<Integer> loadFutureTask = ListenableFutureTask.create(loadCallable);
		Futures.addCallback(loadFutureTask, loadCallback);

		while (conditionChain.isLocked(binlogEvent)) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				stop();
				throw new PumaException("load binlog event failure.", e);
			}
		}

		executorService.submit(loadFutureTask);
	}

	protected Callable<Integer> genLoadTask(final ChangedEvent binlogEvent) {
		final String sql = SqlParser.parseSql(binlogEvent);
		final Object[] params = SqlParser.parseArgs(binlogEvent);

		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				try {
					conditionChain.lock(binlogEvent);
					QueryRunner queryRunner = new QueryRunner(dataSource);
					int result = queryRunner.update(sql, params);
					conditionChain.unlock(binlogEvent);

					return result;
				} catch (SQLException e) {
					stop();
					throw new PumaException("execute sql failure.", e);
				}
			}
		};
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setConditionChain(ConditionChain conditionChain) {
		this.conditionChain = conditionChain;
	}
}
