package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.syncserver.common.binlog.BinlogEvent;
import com.dianping.puma.syncserver.exception.PumaException;
import com.dianping.puma.syncserver.executor.load.condition.*;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;
import java.util.concurrent.*;

public class AsyncLoader extends AbstractLoader {

	protected final int maxConcurrent;

	protected ExecutorService es;

	protected ConditionChain conditionChain;

	public AsyncLoader(int maxConcurrent, ExecutorService es) {
		this.maxConcurrent = maxConcurrent;
		this.es = es;

		initConditionChain();
	}

	private void initConditionChain() {
		conditionChain = new SeriesConditionChain();
		VolCondition volCondition = new VolCondition(maxConcurrent);
		RowCondition rowCondition = new RowCondition();
		DdlCondition ddlCondition = new DdlCondition();
		conditionChain.addCondition(volCondition);
		conditionChain.addCondition(rowCondition);
		conditionChain.addCondition(ddlCondition);
	}

	@Override
	protected void doStart() {
		conditionChain.reset();
	}

	@Override
	protected void doStop() {
	}

	@Override
	public LoadFuture load(BinlogEvent binlogEvent) {
		if (checkStop()) {
			throw new PumaException("load binlog event failure, load module stopped.");
		}

		Callable<Integer> loadCallable = buildLoadTask(binlogEvent);
		LoadFuture loadFuture = new LoadFuture(loadCallable);

		while (conditionChain.isLocked(binlogEvent)) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				stop();
				throw new PumaException("load binlog event failure.", e);
			}
		}

		es.submit(loadFuture);

		return loadFuture;
	}

	protected Callable<Integer> buildLoadTask(final BinlogEvent binlogEvent) {
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				try {
					conditionChain.lock(binlogEvent);

					QueryRunner queryRunner = new QueryRunner(binlogEvent.getDataSource());
					return queryRunner.update(binlogEvent.getSql(), binlogEvent.getParams());
				} catch (SQLException e) {
					stop();
					throw new PumaException("execute sql failure.", e);
				} finally {
					conditionChain.unlock(binlogEvent);
				}
			}
		};
	}
}
