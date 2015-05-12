package com.dianping.puma.syncserver.load;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class PressureTest {

	//DataSource dataSource;
	HikariDataSource dataSource;

	@Before
	public void before() {
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(
				"jdbc:mysql://192.168.224.102:3306/Pressure?useServerPrepStmts=false&rewriteBatchedStatements=true");
		//dataSource.setJdbcUrl("jdbc:mysql://192.168.224.102:3306/");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		//dataSource.setInitialSize(1);
		dataSource.setMaximumPoolSize(2);
		//dataSource.setAutoCommit(false);
	}

	@Test
	@Ignore
	public void test() throws SQLException {
		int n = 1000;
		long duration = 0;
		Connection conn = dataSource.getConnection();

		for (int i = 0; i != n; ++i) {
			long begin = System.currentTimeMillis();
			QueryRunner runner = new QueryRunner(dataSource);
			runner.update(conn, "UPDATE Pressure.business SET name=? where id=?", "bbb", i);
			long end = System.currentTimeMillis();
			duration += (end - begin);
		}

		conn.commit();
		System.out.println("duration = " + duration);
	}

	@Test
	public void testBatch() throws SQLException {
		int n = 1000;
		long duration = 0;
		Object[][] params = new Object[1000][2];
		for (int i = 0; i != 1000; ++i) {
			params[i][0] = "mmm";
			params[i][1] = i;
		}

		Connection conn = null;
		for (int i = 0; i != 1; ++i) {
			long begin = System.currentTimeMillis();
			QueryRunner runner = new QueryRunner(dataSource);
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			runner.batch(conn, "UPDATE Pressure.business SET name=? where id=?", params);
			conn.commit();
			conn.close();
			long end = System.currentTimeMillis();
			duration += (end - begin);
		}

		System.out.println("duration = " + duration);
	}
}
