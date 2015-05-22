package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SCBatchExecPool {

	private static final Logger LOG = LoggerFactory.getLogger(SCBatchExecPool.class);

	/** Pool stopped or not, default true. */
	private boolean stopped = true;

	/** Pool exception, default null. */
	private LoadException loadException = null;

	/** Pool prerequisite settings: data source host. */
	private String host;

	/** Pool prerequisite settings: data source username. */
	private String username;

	/** Pool prerequisite settings: data source password. */
	private String password;

	/** Pool optional settings: pool name, default "SCBatchRowPool-XXXXX". */
	private String name = "BatchExecPool-" + RandomStringUtils.randomAlphabetic(5);

	/** Pool optional settings: pool size, default 1. */
	private int poolSize = 1;

	/** Pool optional settings: sql retries, default 1. */
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


	public SCBatchExecPool() {
	}

	public void start() {
		LOG.info("Starting batch execution pool({})...", name);

		if (!stopped) {
			LOG.warn("Batch execution pool({}) is already started.", name);
		} else {
			// Clear batch execution pool status.
			stopped = false;
			loadException = null;

			// Initial JDBC data source.
			initDataSource();

			// Initial sql execution thread pool.
			initThreadPool();
		}
	}

	public void stop() {
		LOG.info("Stopping batch execution pool({})...", name);

		if (stopped) {
			LOG.warn("batch execution pool({}) is already stopped.", name);
		} else {
			// Clear batch execution pool status.
			stopped = true;

			// Destroy sql execution thread pool.
			destroyThreadPool();

			// Destroy JDBC data source.
			destroyDataSource();
		}
	}

	public void destroy() {
		// No persistent storage.
	}

	public LoadException exception() {
		return loadException;
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

	public void put(BatchRow batchRow) throws InterruptedException {
		lock.lock();
		try {
			buffer = batchRow;
			while (!check(batchRow)) {
				notConflict.await();
			}
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
		if (batchRow.isCommit()) {
			binlogManager.before(batchRow.getBinlogInfo());
		}
	}

	private void afterPooledBatchExecute(BatchRow batchRow) {
		if (batchRow.isCommit()) {
			binlogManager.after(batchRow.getBinlogInfo());
		}
	}

	private void pooledBatchExecute(final BatchRow batchRow) {
		try {
			if (cachedConn == null || cachedConn.isClosed()) {
				cachedConn = dataSource.getConnection();
			}

			pooledBatchExecute(cachedConn, batchRow);
		} catch (SQLException e) {
			if (!stopped) {
				loadException = LoadException.handleException(e);
			}
		}
	}

	private void pooledBatchExecute(final Connection conn, final BatchRow batchRow) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				LoadException te = null;

				for (int count = 0; count <= retries; ++count) {
					try {
						if (batchRow.isCommit()) {
							commit(conn);
						} else {
							batchExecute(conn, batchRow);
						}

						afterPooledBatchExecute(batchRow);
						remove(batchRow);
						return;
					} catch (LoadException e) {
						if (!stopped) {
							te = e;
							continue;
						}
						return;
					}
				}

				// If the code runs to here, then the sql execution is failure.
				loadException = te;
				LOG.error("Sql error: {}.", loadException);
				stop();
			}
		});
	}

	private void batchExecute(Connection conn, BatchRow batchRow) {
		try {
			(new QueryRunner()).batch(conn, batchRow.getSql(), batchRow.getParams());
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw LoadException.handleException(e);
		}
	}

	private void commit(Connection conn) {
		try {
			conn.commit();
			//DbUtils.commitAndClose(conn);
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw LoadException.handleException(e);
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
}
