package com.dianping.puma.syncserver.job.load;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.PumaThreadPool;
import com.dianping.puma.syncserver.job.BinlogInfoManager;
import com.dianping.puma.syncserver.job.load.model.BatchRow;
import com.dianping.puma.syncserver.job.load.model.BatchRowPool;
import com.dianping.puma.syncserver.job.load.model.BatchRowCollision;
import com.dianping.puma.syncserver.job.load.status.LoaderStatus;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class PooledLoader implements Loader {

	private static final Logger LOG = LoggerFactory.getLogger(PooledLoader.class);

	private LoaderStatus status;

	private Thread loadThread;

	private Thread failThread;

	private BatchRowCollision batchRowCollision = new BatchRowCollision(1);

	private BatchRowPool batchRowPool = new BatchRowPool(20);

	private BatchRowPool failBatchRowPool = new BatchRowPool(5);

	private BinlogInfoManager binlogInfoManager;

	//private DataSource dataSource;

	private HikariDataSource dataSource;

	private String host;

	private String username;

	private String password;

	public PooledLoader(String host, String username, String password, BinlogInfoManager binlogInfoManager) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.binlogInfoManager = binlogInfoManager;
	}

	public void start() {
		initDataSource();
		initFailThread();
		failThread.start();
		initLoadThread();
		loadThread.start();

		status = LoaderStatus.RUNNING;
	}

	public void restart() {
		initDataSource();
		initFailThread();
		failThread.start();
		initLoadThread();
		loadThread.start();

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
			LOG.info("LOAD");
			Cat.logEvent("event", "load");
			batchRowPool.put(row);
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
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://" + host + "/?useServerPrepStmts=false&rewriteBatchedStatements=true");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaximumPoolSize(2);
		dataSource.setAutoCommit(false);

		/*
		dataSource = new DataSource();
		dataSource.setUrl("jdbc:mysql://" + host + "/");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaxActive(30);
		dataSource.setInitialSize(20);
		dataSource.setMaxWait(1000);
		dataSource.setMaxIdle(5);
		dataSource.setMinIdle(5);
		dataSource.setRemoveAbandoned(true);
		dataSource.setRemoveAbandonedTimeout(180);*/
	}

	private void initLoadThread() {
		loadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (; ; ) {
					BatchRow batchRow;
					try {
						batchRow = batchRowPool.take();
						batchRowCollision.inject(batchRow);
						pooledExecute(batchRow);
					} catch (InterruptedException e) {
						if (!isHalt()) {
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
					BatchRow batchRow;
					try {
						batchRow = failBatchRowPool.take();
						batchExecute(batchRow);
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

	private void pooledExecute(final BatchRow batchRow) {
		PumaThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					batchExecute(batchRow);
				} catch (SQLException se) {
					if (!isHalt()) {
						LOG.error("Execute row({}) error: {}.", batchRow.toString(), se.getStackTrace());

						try {
							failBatchRowPool.put(batchRow);
						} catch (InterruptedException ie) {
							if (!isHalt()) {
								LOG.error("Put row({}) into fail stash error: {}.", batchRow.toString(), ie.getStackTrace());
								fail();
							}
						}
					}
				}
			}
		});
	}

	private void batchExecute(BatchRow batchRow) throws SQLException {
		Transaction t = Cat.newTransaction("load", "SQL");
		LOG.info("Batch row size = {}.", batchRow.size());

		Connection conn = dataSource.getConnection();
		QueryRunner runner = new QueryRunner();
		try {
			runner.batch(conn, batchRow.getSql(), batchRow.getParams());
			conn.commit();

			// @TODO: record bin log.

			batchRowCollision.extract(batchRow);
		} catch (SQLException e) {
			conn.rollback();
		} finally {
			DbUtils.close(conn);
			t.complete();
		}
	}
}
