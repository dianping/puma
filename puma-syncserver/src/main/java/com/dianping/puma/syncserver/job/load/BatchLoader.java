package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.PumaThreadPool;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.BinlogInfoManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.model.RowPool;
import com.dianping.puma.syncserver.job.load.status.LoaderStatus;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

public class BatchLoader implements Loader {

	private static final Logger LOG = LoggerFactory.getLogger(BatchLoader.class);

	private LoaderStatus status;

	private int batchWidth = 10;

	private Thread loadThread;

	private Thread failThread;

	private RowPool rowPool = new RowPool();

	private ArrayBlockingQueue<ChangedEvent> rowStash = new ArrayBlockingQueue<ChangedEvent>(50);

	private ArrayBlockingQueue<ChangedEvent> failStash = new ArrayBlockingQueue<ChangedEvent>(10);

	private BinlogInfoManager binlogInfoManager;

	private DataSource dataSource;

	private String host;

	private String username;

	private String password;

	public BatchLoader(String host, String username, String password, BinlogInfoManager binlogInfoManager) {
		this.binlogInfoManager = binlogInfoManager;
	}

	public void start() {
		initDataSource();
		initFailThread();
		failThread.run();
		initLoadThread();
		loadThread.run();

		status = LoaderStatus.RUNNING;
	}

	public void restart() {
		initDataSource();
		initFailThread();
		failThread.run();
		initLoadThread();
		loadThread.run();

		status = LoaderStatus.RUNNING;
	}

	public void stop() {
		status = LoaderStatus.STOPPED;

		dataSource.close();
		loadThread.interrupt();
		failThread.interrupt();
	}

	public void pause() {
		status = LoaderStatus.PAUSED;

		dataSource.close();
		loadThread.interrupt();
		failThread.interrupt();
	}

	public void fail() {
		status = LoaderStatus.FAILED;

		dataSource.close();
		loadThread.interrupt();
		failThread.interrupt();
	}

	public void load(ChangedEvent row) {
		try {
			rowStash.put(row);
		} catch (InterruptedException e) {
			if (!isHalt()) {
				LOG.error("Load row({}) error: {}.", row, e.getStackTrace());
				fail();
			}
		}
	}

	private boolean isHalt() {
		return status != LoaderStatus.RUNNING;
	}

	private void initDataSource() {
		DataSource dataSource = new DataSource();
		dataSource.setUrl("jdbc:mysql://" + host + "/");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaxActive(10);
		dataSource.setInitialSize(10);
		dataSource.setMaxWait(1000);
		dataSource.setMaxIdle(5);
		dataSource.setMinIdle(5);
		dataSource.setRemoveAbandoned(true);
		dataSource.setRemoveAbandonedTimeout(180);
	}

	private void initLoadThread() {
		loadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (; ; ) {
					ChangedEvent row;
					try {
						row = rowStash.take();
						rowPool.inject(row);
						pooledExecute(row);
					} catch (InterruptedException e) {
						if (isHalt()) {
							LOG.error("Load thread error: {}.", e.getStackTrace());
							fail();
						}
						break;
					}
				}
			}
		});
	}

	private void initFailThread() {
		failThread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (; ; ) {
					ChangedEvent row;
					try {
						row = failStash.take();
						execute(row);
					} catch (Exception e) {
						if (!isHalt()) {
							LOG.error("Fail thread error: {}.", e.getStackTrace());
							fail();
						}
						break;
					}
				}
			}
		});
	}

	private void pooledExecute(final ChangedEvent row) {
		PumaThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					execute(row);
				} catch (SQLException se) {
					if (!isHalt()) {
						LOG.error("Execute row({}) error: {}.", row.toString(), se.getStackTrace());

						try {
							failStash.put(row);
						} catch (InterruptedException ie) {
							if (!isHalt()) {
								LOG.error("Put row({}) into fail stash error: {}.", row.toString(), ie.getStackTrace());
								fail();
							}
						}
					}
				}
			}
		});
	}

	private void execute(ChangedEvent row) throws SQLException {
		int result = executeSQL(row);

		// Update affects 0 rows, convert it to insert and execute again.
		if (result == 0 && row instanceof RowChangedEvent
				&& ((RowChangedEvent) row).getDMLType() == DMLType.UPDATE) {
			((RowChangedEvent) row).setDMLType(DMLType.INSERT);
			executeSQL(row);
		}

		if (rowPool.isFirst(row)) {
			binlogInfoManager.setFirst(new BinlogInfo(row.getBinlog(), row.getBinlogPos()));
		}
		rowPool.extract(row);
	}

	private int executeSQL(ChangedEvent event) {
		if (event instanceof DdlEvent) {
			return executeDDL((DdlEvent) event);
		} else {
			return executeDML((RowChangedEvent) event);
		}
	}

	private int executeDML(RowChangedEvent dml) {
		try {
			// @TODO: should be done in transformer.
			if (dml.getDMLType() == DMLType.INSERT) {
				dml.setDMLType(DMLType.REPLACE);
			}

			String sql = LoadParser.parseSql(dml);
			Object[] args = LoadParser.parseArgs(dml);

			QueryRunner runner = new QueryRunner(dataSource);
			return runner.update(sql, args);
		} catch (SQLException e) {
			LOG.error("Execute ({}) error: {}.", dml.toString(), e.getStackTrace());
			throw new LoadException(String.valueOf(e.getErrorCode()), e.getMessage(), e.getCause());
		}
	}

	private int executeDDL(DdlEvent ddl) {
		try {
			String sql = ddl.getSql();
			QueryRunner runner = new QueryRunner(dataSource);
			return runner.update(sql);
		} catch (SQLException e) {
			LOG.error("Execute ({}) error: {}.", ddl.toString(), e.getStackTrace());
			throw new LoadException(String.valueOf(e.getErrorCode()), e.getMessage(), e.getCause());
		}
	}
}
