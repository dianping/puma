package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.util.PumaThreadPool;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public class DataSourceTest {

	@Test
	public void testC3P0() {

	}

	/*
	@Test
	@Ignore
	public void testTomcatJDBC() {
		final DataSource dataSource = new DataSource();
		dataSource.setUrl("jdbc:mysql://192.168.224.102:3306/");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaxActive(10);
		dataSource.setInitialSize(10);
		dataSource.setMaxWait(1000);
		dataSource.setMaxIdle(5);
		dataSource.setMinIdle(5);
		dataSource.setRemoveAbandoned(true);
		dataSource.setRemoveAbandonedTimeout(180);

		final CountDownLatch latch = new CountDownLatch(2);

		for (int j = 0; j != 2; ++j) {
			PumaThreadPool.execute(new Runnable() {
				@Override public void run() {
					try {
						long diff = 0;
						for (int i = 0; i != 1000; ++i) {
							long begin = System.currentTimeMillis();
							QueryRunner runner = new QueryRunner(dataSource);
							runner.update("INSERT Pressure.business (name) VALUES (?)", "aaa");
							//runner.update("UPDATE Pressure.business SET name = ? WHERE name = ?", "bbb", "ccc");
							long end = System.currentTimeMillis();
							diff += (end - begin);
						}
						System.out.println(diff / 1000);
						//Assert.assertEquals(1, result);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					latch.countDown();
				}
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/

	@Test
	public void testHikariCP() {
		final HikariDataSource dataSource = new HikariDataSource();
		//dataSource.setJdbcUrl("jdbc:mysql://192.168.224.102:3306/?useServerPrepStmts=false&rewriteBatchedStatements=true");
		dataSource.setJdbcUrl("jdbc:mysql://10.128.53.21:3307/?useServerPrepStmts=false&rewriteBatchedStatements=true");
		dataSource.setUsername("root");
		dataSource.setPassword("admin");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaximumPoolSize(1);
		dataSource.setAutoCommit(false);


		Object[][] params0 = {{14, "2015-04-28 11:36:02.030", "11:36:00.3303","2015-04-28 11:36:02.3300"}};
		Object[][] params1 = {{23, "user"}};
		try {
			Connection conn = dataSource.getConnection();
			QueryRunner runner = new QueryRunner();
			runner.batch(conn, "INSERT INTO \n"
					+ "`testdb`.`tbl_test`\n"
					+ "(\n"
					+ "  `id`\n"
					+ "        ,\n"
					+ "    `createTime`\n"
					+ "        ,\n"
					+ "    `updateTime`\n"
					+ "        ,\n"
					+ "    `lastTime`\n"
					+ "    ) \n"
					+ "VALUES \n"
					+ "(?,?,?,?)\n"
					+ "ON DUPLICATE KEY UPDATE\n"
					+ "                                                       createTime=VALUES(createTime),updateTime=VALUES(updateTime),lastTime=VALUES(lastTime) ", params0);

			conn.commit();
			//runner.update("INSERT Pressure.business (name) VALUES (?)", "aaa");

			runner.batch(conn, "INSERT INTO \n"
					+ "`testdb`.`tbl_user`\n"
					+ "(\n"
					+ "  `id`\n"
					+ "        ,\n"
					+ "    `name`\n"
					+ "    ) \n"
					+ "VALUES \n"
					+ "(?,?)\n"
					+ "ON DUPLICATE KEY UPDATE \n"
					+ "                       `name`=VALUES(name)", params1);

			conn.commit();
			/*
			runner.batch("INSERT INTO \n"
					+ "`Pressure`.`business`\n"
					+ "(\n"
					+ "  `id`\n"
					+ "        ,\n"
					+ "    `name`\n"
					+ "    ) \n"
					+ "VALUES \n"
					+ "(?,?)\n"
					+ "ON DUPLICATE KEY UPDATE `name`=?", params);*/
			//runner.batch("INSERT Pressure.business (`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `name` = ?", params);
		} catch (SQLException e) {
			e.printStackTrace();
		}


		/*
		final CountDownLatch latch = new CountDownLatch(10);

		for (int j = 0; j != 10; ++j) {
			PumaThreadPool.execute(new Runnable() {
				@Override public void run() {
					try {
						long diff = 0;
						for (int i = 0; i != 1000; ++i) {
							long begin = System.currentTimeMillis();
							QueryRunner runner = new QueryRunner(dataSource);
							runner.update("INSERT Pressure.business (name) VALUES (?)", "aaa");
							//runner.update("UPDATE Pressure.business SET name = ? WHERE name = ?", "bbb", "ccc");
							long end = System.currentTimeMillis();
							diff += (end - begin);
						}
						System.out.println(diff / 1000);
						//Assert.assertEquals(1, result);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					latch.countDown();
				}
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}
}
