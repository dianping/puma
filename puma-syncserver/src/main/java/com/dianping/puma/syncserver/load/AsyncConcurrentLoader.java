package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.exception.PumaException;
import com.dianping.puma.syncserver.load.condition.ConditionChain;
import com.dianping.puma.syncserver.util.SqlParser;
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
	public LoadFuture load(ChangedEvent binlogEvent) {
		if (checkStop()) {
			throw new PumaException("load binlog event failure, stopped.");
		}

		Callable<Integer> loadTask = genLoadTask(binlogEvent);
		LoadFuture loadFuture = new LoadFuture(loadTask);

		while (conditionChain.isLocked(binlogEvent)) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				stop();
				throw new PumaException("load binlog event failure.", e);
			}
		}

		executorService.submit(loadFuture);

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
}
