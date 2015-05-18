package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.monitor.TransactionMonitor;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.load.model.BatchRow;
import com.dianping.puma.syncserver.job.load.model.RowKey;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BatchExecPool {

	private static final Logger LOG = LoggerFactory.getLogger(BatchExecPool.class);

	private String name = "name";

	// Pool status.
	private boolean stopped = true;
	private Exception exception = null;

	// Pool size.
	private int poolSize;
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
	private String host;
	private String username;
	private String password;

	// JDBC retry times.
	private int retires;

	private BinlogManager binlogManager;

	// Used to block the put operation.
	private Lock lock = new ReentrantLock();
	private final Condition notConflict = lock.newCondition();

	// Monitor.
	private TransactionMonitor execMonitor = new TransactionMonitor("SQL.execute", 1L);
	private TransactionMonitor connMonitor = new TransactionMonitor("SQL.connection", 1L);


	public BatchExecPool() {
	}

	public void start() {
		LOG.info("BatchExecPool({}) is starting.", name);

		initThreadPool();
		initDataSource();
		//execMonitor.start();
		//connMonitor.start();
		stopped = false;
	}

	public void stop() {
		LOG.info("BatchExecPool({}) is stopping.", name);

		stopped = true;
		destroyThreadPool();
		destroyDataSource();
		//execMonitor.stop();
		//connMonitor.stop();
	}

	public boolean hasException() {
		return exception != null;
	}

	public Exception getException() {
		return exception;
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

	@ThreadSafe
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

	private void pooledBatchExecute(final BatchRow batchRow) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				Exception te = null;

				for (int count = 0; count <= retires; ++count) {
					try {
						batchExecute(batchRow);
						remove(batchRow);
						binlogManager.save(batchRow.getBinlogInfo());
						return;
					} catch (SQLException e) {
						if (!stopped) {
							te = e;
							continue;
						}
						return;
					}
				}

				// If the code runs to here, then the sql execution is failure.
				exception = te;
				LOG.error("Sql error: {}.", exception);
				stop();
			}
		});
	}

	private void batchExecute(BatchRow batchRow) throws SQLException {
		Transaction t = Cat.newTransaction("SQL", "execute");
		Connection conn = dataSource.getConnection();
		try {
			(new QueryRunner()).batch(conn, batchRow.getSql(), batchRow.getParams());
			DbUtils.commitAndClose(conn);
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			t.complete();
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

	public void setRetires(int retires) {
		this.retires = retires;
	}

	public void setBinlogManager(BinlogManager binlogManager) {
		this.binlogManager = binlogManager;
	}
}
