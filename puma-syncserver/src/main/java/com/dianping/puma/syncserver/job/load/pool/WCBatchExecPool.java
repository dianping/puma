package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import com.dianping.puma.syncserver.job.load.row.RowKey;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WCBatchExecPool implements BatchExecPool {

	private static final Logger LOG = LoggerFactory.getLogger(WCBatchExecPool.class);

	// Pool status.
	private boolean stopped = true;
	private LoadException loadException = null;

	// Pool prerequisite settings.
	private String host;
	private String username;
	private String password;

	// Pool optional settings.
	private String name = "BatchExecPool-" + RandomStringUtils.randomAlphabetic(5);
	private int poolSize = 1;
	private boolean isStrongConsistency = false;
	private int retries = 1;

	// Connection cache.
	private Connection cachedConn;

	private boolean needNewConnection = true;

	private volatile int dmlSize = 0;
	private volatile int ddlSize = 0;

	// Bottom implementation.
	private ConcurrentMap<RowKey, Boolean> rowKeys = new ConcurrentHashMap<RowKey, Boolean>();

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


	public WCBatchExecPool() {
	}

	@Override
	public void init() {
		loadException = null;

		initDataSource();
		initThreadPool();
	}

	@Override
	public void destroy() {
		destroyThreadPool();
		destroyDataSource();
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	private void asyncThrow() throws LoadException {
		if (loadException != null) {
			throw loadException;
		}
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

	@Override
	public void put(BatchRow batchRow) throws LoadException {
		lock.lock();
		try {
			buffer = batchRow;
			while (!check(batchRow)) {
				notConflict.await();
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

	@ThreadSafe
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

	@ThreadSafe
	private boolean check(BatchRow batchRow) {
		if (batchRow.isDdl()) {
			return dmlSize == 0 && ddlSize == 0;
		} else {
			if (dmlSize + ddlSize >= poolSize) {
				return false;
			}
			if (ddlSize > 0) {
				return false;
			}
			for (RowKey rowKey : batchRow.getRowKeys().keySet()) {
				if (rowKeys.containsKey(rowKey)) {
					return false;
				}
			}
			return true;
		}
	}

	@ThreadUnSafe
	private void register(BatchRow batchRow) {
		if (batchRow.isDdl()) {
			++ddlSize;
		} else {
			++dmlSize;
			rowKeys.putAll(batchRow.getRowKeys());
		}
	}

	@ThreadUnSafe
	private void unregister(BatchRow batchRow) {
		if (batchRow.isDdl()) {
			--ddlSize;
		} else {
			--dmlSize;
			for (RowKey rowKey : batchRow.getRowKeys().keySet()) {
				rowKeys.remove(rowKey);
			}
		}
	}

	private void beforePooledBatchExecute(BatchRow batchRow) {
		if (isStrongConsistency) {
			if (batchRow.isCommit()) {
				binlogManager.before(batchRow.getSeq(), batchRow.getBinlogInfo());
			}
		} else {
			binlogManager.before(batchRow.getSeq(), batchRow.getBinlogInfo());
		}
	}

	private void afterPooledBatchExecute(BatchRow batchRow) {
		if (isStrongConsistency) {
			if (batchRow.isCommit()) {
				binlogManager.after(batchRow.getSeq(), batchRow.getBinlogInfo());
			}
		} else {
			binlogManager.after(batchRow.getSeq(), batchRow.getBinlogInfo());
		}
	}

	private void pooledBatchExecute(final BatchRow batchRow) {
		try {
			Connection conn;

			if (isStrongConsistency) {
				if (needNewConnection) {
					conn = dataSource.getConnection();
				} else {
					conn = cachedConn;
				}
			} else {
				conn = dataSource.getConnection();
			}

			pooledBatchExecute(conn, batchRow);
		} catch (SQLException e) {
			if (!stopped) {
				loadException = LoadException.translate(e);
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

						if (isStrongConsistency) {
							if (batchRow.isCommit()) {
								commit(conn);
							} else {
								batchExecute(conn, batchRow);
							}
						} else {
							batchExecute(conn, batchRow);
							commit(conn);
						}
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
			}
		});
	}

	private void batchExecute(Connection conn, BatchRow batchRow) {
		try {
			(new QueryRunner()).batch(conn, batchRow.getSql(), batchRow.getParams());
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw LoadException.translate(e);
		}
	}

	private void commit(Connection conn) {
		try {
			DbUtils.commitAndClose(conn);
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw LoadException.translate(e);
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

	public void setStrongConsistency(boolean isTransactionDisable) {
		this.isStrongConsistency = isTransactionDisable;
	}

	public void setBinlogManager(BinlogManager binlogManager) {
		this.binlogManager = binlogManager;
	}
}
