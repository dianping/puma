package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.PumaThreadPool;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.model.BatchRows;
import com.dianping.puma.syncserver.job.load.model.RowKey;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public class BatchLoader implements Loader {

	private int width = 5;

	private BatchRows batchRows = new BatchRows();

	private volatile boolean batchSuccess = true;

	private ComboPooledDataSource dataSource = null;

	public BatchLoader(String host, int port, String username, String password) {
		initDataSource(host, port, username, password);
	}

	private void initDataSource(String host, int port, String username, String password) {
		try {
			dataSource = new ComboPooledDataSource();
			dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/");
			dataSource.setUser(username);
			dataSource.setPassword(password);
			dataSource.setDriverClass("com.mysql.jdbc.Driver");
			dataSource.setMinPoolSize(5);
			dataSource.setMaxPoolSize(5);
			dataSource.setInitialPoolSize(5);
			dataSource.setMaxIdleTime(300);
			dataSource.setIdleConnectionTestPeriod(60);
			dataSource.setAcquireRetryAttempts(3);
			dataSource.setAcquireRetryDelay(300);
			dataSource.setMaxStatements(0);
			dataSource.setMaxStatementsPerConnection(100);
			dataSource.setNumHelperThreads(6);
			dataSource.setMaxAdministrativeTaskTime(5);
			dataSource.setPreferredTestQuery("SELECT 1");
			dataSource.setTestConnectionOnCheckin(true);
		} catch (PropertyVetoException e) {
			throw new IllegalStateException(e.getCause());
		}
	}

	public void load(ChangedEvent event) throws LoadException {
		if (event instanceof DdlEvent) {
			batchExecute();

			DdlEvent ddl = (DdlEvent) event;
			execute(ddl);
		} else {
			RowChangedEvent row = (RowChangedEvent) event;

			LoadMerger.merge(row, batchRows);
			if (batchRows.size() >= width) {
				batchExecute();
			}
		}
	}

	private void batchExecute() {
		batchSuccess = true;
		final CountDownLatch latch = new CountDownLatch(batchRows.size());

		for (final RowChangedEvent row: batchRows.listRows()) {
			PumaThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						int affectedRows = execute(row);

						// If no row is updated by the UPDATE statement, use INSERT instead.
						if (row.getDMLType() == DMLType.UPDATE && affectedRows == 0) {
							row.setDMLType(DMLType.INSERT);
							execute(row);
						}

						// If sql is success, removes it.
						batchRows.remove(RowKey.getNewRowKey(row));
					} catch (Exception e) {
						batchSuccess = false;
					} finally {
						latch.countDown();
					}
				}
			});
		}

		try {
			latch.await();

			if (!batchSuccess) {
				throw new LoadException("0");
			}

		} catch (InterruptedException e) {
			// @TODO
			throw new LoadException("0");
		}
	}

	private int execute(ChangedEvent event) {
		if (event instanceof DdlEvent) {
			return executeDDL((DdlEvent) event);
		} else {
			return executeDML((RowChangedEvent) event);
		}
	}

	private int executeDML(RowChangedEvent dml) {
		try {
			String sql = LoadParser.parseSql(dml);
			Object[] args = LoadParser.parseArgs(dml);
			QueryRunner run = new QueryRunner(dataSource);
			return run.update(sql, args);
		} catch (SQLException e) {
			throw new LoadException(String.valueOf(e.getErrorCode()), e.getMessage(), e.getCause());
		}
	}

	private int executeDDL(DdlEvent ddl) {
		try {
			String sql = ddl.getSql();
			QueryRunner runner = new QueryRunner(dataSource);
			return runner.update(sql);
		} catch (SQLException e) {
			throw new LoadException(String.valueOf(e.getErrorCode()), e.getMessage(), e.getCause());
		}
	}
}
