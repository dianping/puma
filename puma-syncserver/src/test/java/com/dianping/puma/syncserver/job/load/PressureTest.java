package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.PumaThreadPool;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import com.dianping.puma.syncserver.job.load.pool.BatchExecPool;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class PressureTest {

	BatchExecPool batchExecPool;

	static long duration = 0;
	static long count = 0;

	@Before
	public void before() {
		batchExecPool = new BatchExecPool();
		batchExecPool.setHost("192.168.224.98:3306");
		batchExecPool.setUsername("root");
		batchExecPool.setPassword("123456");
		batchExecPool.setPoolSize(7);
		batchExecPool.setRetires(0);
		batchExecPool.start();
	}

	@Test
	public void test() throws InterruptedException {
		int n = 1000000;
		for (int i = 0; i != n; ++i) {
			RowChangedEvent row = new RowChangedEvent();
			row.setDatabase("Pressure");
			row.setTable("user");
			row.setDmlType(DMLType.UPDATE);
			Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = new HashMap<String, RowChangedEvent.ColumnInfo>();
			columnInfoMap.put("id", new RowChangedEvent.ColumnInfo(true, i, i));
			columnInfoMap.put("name", new RowChangedEvent.ColumnInfo(false, "zzz", "jjj"));
			row.setColumns(columnInfoMap);

			BatchRow batchRow = new BatchRow();
			batchRow.addRow(row);

			long begin = System.currentTimeMillis();
			batchExecPool.put(batchRow);
			long end = System.currentTimeMillis();
			duration += (end - begin);
			System.out.println((i + 1) + ":" + duration);
		}
	}

	@Test
	@Ignore
	public void testSelect() throws InterruptedException {
		final HikariDataSource dataSource = new HikariDataSource();

		String host = "192.168.224.98:3306";
		String username = "root";
		String password = "123456";
		int poolSize = 5;

		dataSource.setJdbcUrl("jdbc:mysql://" + host + "/?useServerPrepStmts=false&rewriteBatchedStatements=true");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaximumPoolSize(poolSize);
		dataSource.setAutoCommit(false);

		final BlockingQueue<Object[]> bq = new ArrayBlockingQueue<Object[]>(100);
		final BlockingDeque<Object[]> bd = new LinkedBlockingDeque<Object[]>(poolSize);
		//final String sql = "SELECT id, name FROM Pressure.user WHERE id = ?";
		final String sql = "UPDATE Pressure.user SET name = ? WHERE id = ?";

		int n = 1000000;

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					try {
						final Object[] o = bq.take();

						long begin = System.currentTimeMillis();
						bd.put(o);
						long end = System.currentTimeMillis();
						duration += (end - begin);
						System.out.println((++count) + ":"  + duration);

						PumaThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								QueryRunner runner = new QueryRunner(dataSource);

								ResultSetHandler<Object> rs = new ResultSetHandler<Object>() {
									@Override public Object handle(ResultSet resultSet) throws SQLException {
										return 1;
									}
								};
								Connection conn = null;
								try {

									//runner.query(sql, rs, o);
									conn = dataSource.getConnection();
									runner.update(conn, sql, o);
									conn.commit();
									bd.remove(o);
								} catch (SQLException e) {
									e.printStackTrace();
								} finally {
									try {
										conn.close();
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		});

		t.start();

		for (int i = 0; i != n; ++i) {
			bq.put(new Object[] { "aaa", i });
		}
	}
}
