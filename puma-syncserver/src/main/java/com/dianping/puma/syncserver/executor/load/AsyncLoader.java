package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.common.binlog.BinlogEvent;
import com.dianping.puma.syncserver.exception.PumaException;
import com.dianping.puma.syncserver.executor.load.condition.*;
import com.dianping.puma.syncserver.util.sql.SqlParser;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.*;

public class AsyncLoader extends AbstractLoader {

	protected final int maxConcurrent;

	protected DataSource ds;

	protected ExecutorService es;

	protected ConditionChain conditionChain;

	public AsyncLoader(int maxConcurrent, DataSource ds, ExecutorService es) {
		this.maxConcurrent = maxConcurrent;
		this.ds = ds;
		this.es = es;
	}

	@Override
	protected void doStart() {
		conditionChain = new SeriesConditionChain();
		VolCondition volCondition = new VolCondition(maxConcurrent);
		RowCondition rowCondition = new RowCondition();
		DdlCondition ddlCondition = new DdlCondition();
		conditionChain.addCondition(volCondition);
		conditionChain.addCondition(rowCondition);
		conditionChain.addCondition(ddlCondition);
		conditionChain.reset();
	}

	@Override
	protected void doStop() {
		conditionChain = null;
	}

	@Override
	public LoadFuture load(BinlogEvent binlogEvent) {
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

		es.submit(loadFuture);

		return loadFuture;
	}

	protected Callable<Integer> genLoadTask(final BinlogEvent binlogEvent) {
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				try {
					String sql = binlogEvent.getSql();
					Object[] params = binlogEvent.getParams();

					//conditionChain.lock(binlogEvent);
					QueryRunner queryRunner = new QueryRunner(ds);
					return queryRunner.update(sql, params);
				} catch (SQLException e) {
					stop();
					throw new PumaException("execute sql failure.", e);
				} finally {
					//conditionChain.unlock(binlogEvent);
				}
			}
		};
	}
}
