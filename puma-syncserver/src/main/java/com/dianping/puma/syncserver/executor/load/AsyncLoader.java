package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.exception.PumaException;
import com.dianping.puma.syncserver.executor.load.condition.ConditionChain;
import com.dianping.puma.syncserver.util.sql.SqlParser;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.*;

public class AsyncLoader extends AbstractLoader {

	// Injected.
	protected DataSource dataSource;

	// Injected.
	protected ExecutorService sqlExecutorThreadPool;

	protected ConditionChain conditionChain;

	public AsyncLoader(int maxConcurrent, DataSource dataSource, ExecutorService sqlExecutorThreadPool) {

	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public LoadFuture load(ChangedEvent binlogEvent) {
		if (checkStop()) {
			throw new PumaException("load binlog event failure, load module stopped.");
		}

		Callable<Integer> loadCallable = genLoadTask(binlogEvent);
		LoadFuture loadFuture = new LoadFuture(loadCallable);

		while (conditionChain.isLocked(binlogEvent)) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				stop();
				throw new PumaException("load binlog event failure.", e);
			}
		}

		sqlExecutorThreadPool.submit(loadFuture);

		return loadFuture;
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
					return queryRunner.update(sql, params);
				} catch (SQLException e) {
					stop();
					throw new PumaException("execute sql failure.", e);
				} finally {
					conditionChain.unlock(binlogEvent);
				}
			}
		};
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setSqlExecutorThreadPool(ExecutorService sqlExecutorThreadPool) {
		this.sqlExecutorThreadPool = sqlExecutorThreadPool;
	}

	public void setConditionChain(ConditionChain conditionChain) {
		this.conditionChain = conditionChain;
	}
}
