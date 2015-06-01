package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.cat.Cat;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SCBatchExecPool implements BatchExecPool {

	private static final Logger LOG = LoggerFactory.getLogger(SCBatchExecPool.class);

	private volatile LoadException loadException = null;

	/** Pool prerequisite settings: data source host. */
	private String host;

	/** Pool prerequisite settings: data source username. */
	private String username;

	/** Pool prerequisite settings: data source password. */
	private String password;

	/** Batch exec pool size, always 1. */
	private int poolSize = 1;

	/** Batch exec pool sql retries, default 1. */
	private int retries = 1;

	private Connection cachedConn;

	/** Pool current size. */
	private int size = 0;

	// Batch row which is blocked.
	private BatchRow buffer;

	// Thread pool.
	private ExecutorService threadPool;

	// JDBC connection pool.
	private HikariDataSource dataSource;

	private BinlogManager binlogManager;

	// Used to block the put operation.
	private Lock lock = new ReentrantLock();
	private final Condition notConflict = lock.newCondition();

	private Thread warnDelayThread;

	/** Binlog events delay statistics. */
	private AtomicLong delay;

	/** Update statistics. */
	private AtomicLong updates;

	/** Insert statistics. */
	private AtomicLong inserts;

	/** Delete statistics. */
	private AtomicLong deletes;

	/** DDL statistics. */
	private AtomicLong ddls;

	public SCBatchExecPool() {}

	@Override
	public void init() {
		loadException = null;

		initDataSource();
		initThreadPool();

		warnDelayThread = new Thread(new WarnDelayTask());
	}

	@Override
	public void destroy() {
		warnDelayThread = null;

		destroyThreadPool();
		destroyDataSource();
	}

	@Override
	public void start() {
		warnDelayThread.start();
	}

	@Override
	public void stop() {
		warnDelayThread.interrupt();
	}

	@Override
	public void asyncThrow() throws LoadException {
		if (loadException != null) {
			throw loadException;
		}
	}

	@Override
	public void put(BatchRow batchRow) throws LoadException {
		lock.lock();
		try {
			buffer = batchRow;
			while (!check(batchRow)) {
				notConflict.await();
			}

			if (loadException != null) {
				throw loadException;
			}

			buffer = null;
			register(batchRow);
		} catch (Exception e) {
			throw LoadException.translate(e);
		} finally {
			lock.unlock();
		}


		beforePooledBatchExecute(batchRow);
		pooledBatchExecute(batchRow);
	}

	private void initThreadPool() {
		threadPool = Executors.newCachedThreadPool();
	}

	private void destroyThreadPool() {
		threadPool.shutdown();
		threadPool = null;
	}

	private void initDataSource() {
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://" + host + "/?useServerPrepStmts=false&rewriteBatchedStatements=true");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaximumPoolSize(poolSize);
		dataSource.setAutoCommit(false);
	}

	private void destroyDataSource() {
		dataSource.close();
		dataSource = null;
	}

	private void remove(BatchRow batchRow) {
		lock.lock();
		try {
			unregister(batchRow);
			if (buffer != null && check(buffer)) {
				notConflict.signal();
			}
		} finally {
			lock.unlock();
		}
	}

	private boolean check(BatchRow batchRow) {
		return size < poolSize;
	}

	private void register(BatchRow batchRow) {
		++size;
	}

	private void unregister(BatchRow batchRow) {
		--size;
	}

	private void beforePooledBatchExecute(BatchRow batchRow) {
		if (batchRow.isCommit()) {
			binlogManager.before(batchRow.getSeq(), batchRow.getBinlogInfo());
		}
	}

	private void afterPooledBatchExecute(BatchRow batchRow) {
		if (batchRow.isCommit() || batchRow.isDdl()) {
			binlogManager.after(batchRow.getSeq(), batchRow.getBinlogInfo());
		}
	}

	private void doStatistic(BatchRow batchRow) {
		// Binlog events delay.
		delay.set(System.currentTimeMillis() - batchRow.getExecuteTime());

		if (batchRow.isCommit()) {
			// Do nothing.
		} else if (batchRow.isDdl()) {
			ddls.incrementAndGet();
		} else {
			switch (batchRow.getDmlType()) {
			case UPDATE:
				updates.addAndGet(batchRow.size());
				break;
			case INSERT:
				inserts.addAndGet(batchRow.size());
				break;
			case DELETE:
				deletes.addAndGet(batchRow.size());
				break;
			}
		}
	}

	private class WarnDelayTask implements Runnable {

		@Override
		public void run() {
			if (delay.get() > 30 * 1000) {
				Cat.logError("Binlog delay too much.", new LoadException(0));
			}
		}
	}

	private void pooledBatchExecute(BatchRow batchRow) {
		BatchExecuteThread batchExecuteThread = new BatchExecuteThread();
		batchExecuteThread.setBatchRow(batchRow);

		threadPool.execute(batchExecuteThread);
	}

	private class BatchExecuteThread implements Runnable {

		private BatchRow batchRow;

		public void setBatchRow(BatchRow batchRow) {
			this.batchRow = batchRow;
		}

		private Connection getConnection() {
			try {
				if (cachedConn == null || cachedConn.isClosed()) {
					cachedConn = dataSource.getConnection();
				}

				return cachedConn;
			} catch (SQLException e) {
				throw LoadException.translate(e);
			}
		}

		private void batchExecute(Connection conn, BatchRow batchRow) {
			try {
				int[] affected = (new QueryRunner()).batch(conn, batchRow.getSql(), batchRow.getParams());

				if (batchRow.getDmlType() == DMLType.UPDATE) {
					for (int i = 0; i != affected.length; ++i) {
						if (affected[i] == 0) {
							(new QueryRunner()).batch(conn, batchRow.getU2iSql(), batchRow.getU2iParams());
							break;
						}
					}
				}
			} catch (SQLException e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				throw LoadException.translate(e);
			}
		}

		private void executeDdl(Connection conn, BatchRow batchRow) {
			try {
				(new QueryRunner()).update(conn, batchRow.getSql());
			} catch (SQLException e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				throw LoadException.translate(e);
			}
		}

		private void commit(Connection conn) {
			try {
				conn.commit();
				//DbUtils.commitAndClose(conn);
			} catch (SQLException e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				throw LoadException.translate(e);
			}
		}

		@Override
		public void run() {
			LoadException e = null;

			for (int count = 0; count <= retries; ++count) {
				try {

					Connection conn = getConnection();

					if (batchRow.isCommit()) {
						commit(conn);
					} else if (batchRow.isDdl()) {
						executeDdl(conn, batchRow);
						commit(conn);
					} else {
						batchExecute(conn, batchRow);
					}

					doStatistic(batchRow);
					afterPooledBatchExecute(batchRow);
					remove(batchRow);

					return;
				} catch (LoadException le) {
					String msg = String.format("Executing batch row(%s) error.", batchRow.toString());
					e = le;
				}
			}

			if (e != null) {
				loadException = e;
				remove(batchRow);
			}
		}
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRetires(int retries) {
		this.retries = retries;
	}

	public void setBinlogManager(BinlogManager binlogManager) {
		this.binlogManager = binlogManager;
	}

	public void setDelay(AtomicLong delay) {
		this.delay = delay;
	}

	public void setUpdates(AtomicLong updates) {
		this.updates = updates;
	}

	public void setInserts(AtomicLong inserts) {
		this.inserts = inserts;
	}

	public void setDeletes(AtomicLong deletes) {
		this.deletes = deletes;
	}

	public void setDdls(AtomicLong ddls) {
		this.ddls = ddls;
	}
}
