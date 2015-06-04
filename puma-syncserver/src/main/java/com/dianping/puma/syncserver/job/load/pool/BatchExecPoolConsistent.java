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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BatchExecPoolConsistent implements BatchExecPool {

	private static final Logger logger = LoggerFactory.getLogger(BatchExecPoolConsistent.class);

	private boolean inited = false;
	private volatile boolean stopped = true;
	private volatile LoadException loadException = null;

	private String name;

	private BinlogManager binlogManager;
	private HikariDataSource dataSource;
	private ExecutorService threadPool;

	private String host;
	private String username;
	private String password;

	private int poolSize = 1;

	private int retries = 1;

	private Connection connCached;

	/** Pool current size. */
	private int size = 0;

	// Batch row which is blocked.
	private BatchRow buffer;

	// Used to block the put operation.
	private Lock lock = new ReentrantLock();
	private final Condition notConflict = lock.newCondition();

	private AtomicLong lagSeconds;
	private AtomicLong updates;
	private AtomicLong inserts;
	private AtomicLong deletes;
	private AtomicLong ddls;

	public BatchExecPoolConsistent() {}

	@Override
	public void init() {
		if (inited) {
			return;
		}

		binlogManager.init();
		initDataSource();
		initConnection();
		initThreadPool();

		inited = true;
	}

	@Override
	public void destroy() {
		if (!inited) {
			return;
		}

		destroyThreadPool();
		destroyConnection();
		destroyDataSource();
		binlogManager.destroy();

		inited = false;
	}

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		stopped = false;

		binlogManager.start();
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;

		binlogManager.stop();
	}

	private void asyncThrow() throws LoadException {
		if (loadException != null) {
			throw loadException;
		}
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

	private void initConnection() {
		try {
			if (connCached == null || connCached.isClosed()) {
				connCached = dataSource.getConnection();
			}
		} catch (SQLException e) {
			String msg = String.format("Batch exec pool(%s) initializing connection failure.", name);
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void initThreadPool() {
		threadPool = Executors.newCachedThreadPool();
	}

	private void destroyDataSource() {
		dataSource.close();
		dataSource = null;
	}

	private void destroyConnection() {
		try {
			if (connCached != null && !connCached.isClosed()) {
				connCached.close();
			}
		} catch (SQLException e) {
			String msg = String.format("Batch exec pool(%s) destroying connection failure.", name);
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void destroyThreadPool() {
		threadPool.shutdown();
		threadPool = null;
	}

	@Override
	public void put(BatchRow batchRow) throws InterruptedException {
		lock.lock();
		try {
			buffer = batchRow;
			while (!check(batchRow)) {
				notConflict.await();
			}

			// Async throw exception, spread the exception out.
			asyncThrow();

			buffer = null;
			register(batchRow);
		} finally {
			lock.unlock();
		}

		beforePooledBatchExecute(batchRow);
		pooledBatchExecute(batchRow);
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
		if (batchRow.isCommit() || batchRow.isDdl()) {
			binlogManager.before(batchRow.getSeq(), batchRow.getBinlogInfo());
		}
	}

	private void afterPooledBatchExecute(BatchRow batchRow) {
		if (batchRow.isCommit() || batchRow.isDdl()) {
			binlogManager.after(batchRow.getSeq(), batchRow.getBinlogInfo());
		}
	}

	private void doStatistic(BatchRow batchRow) {
		// Binlog events lagSeconds.
		lagSeconds.set(System.currentTimeMillis() - batchRow.getExecuteTime());

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
				if (connCached == null || connCached.isClosed()) {
					connCached = dataSource.getConnection();
				}

				return connCached;
			} catch (SQLException e) {
				throw LoadException.translate(e);
			}
		}

		private void executeDml(Connection conn, BatchRow batchRow) {
			try {
				int[] affected = (new QueryRunner()).batch(conn, batchRow.getSql(), batchRow.getParams());


			} catch (SQLException e) {

			}
		}

		private void batchExecute(Connection conn, BatchRow batchRow) {
			logger.info("Batch execute rows({}).", batchRow.toString());

			try {
				int[] affected = (new QueryRunner()).batch(conn, batchRow.getSql(), batchRow.getParams());
				logger.info("Batch execute affect rows = {}.", affected[0]);

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
			logger.info("Commit.");

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
					logger.error(msg, le);
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

	public void setLagSeconds(AtomicLong lagSeconds) {
		this.lagSeconds = lagSeconds;
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
