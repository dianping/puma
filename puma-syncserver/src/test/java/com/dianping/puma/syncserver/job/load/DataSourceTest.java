package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.util.PumaThreadPool;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public class DataSourceTest {

	@Test
	public void testC3P0() {

	}

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
	}

	@Test
	public void testHikariCP() {
		final HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setMaximumPoolSize(10);

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
		}
	}
}
