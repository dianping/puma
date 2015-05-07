package com.dianping.puma.syncserver.load;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class BatchLoaderTest {

	@Test
	public void testC3P0() {

	}

	@Test
	public void testTomcatJDBC() {
		DataSource dataSource = new DataSource();
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

		QueryRunner runner = new QueryRunner(dataSource);
		try {
			int result = runner.update("UPDATE Pressure.user SET name = ? WHERE id = ?", "jjj", 10);
			Assert.assertEquals(1, result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
