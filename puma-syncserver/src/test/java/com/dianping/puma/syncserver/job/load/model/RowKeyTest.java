package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.util.sql.DMLType;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Assert;
import org.junit.Test;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RowKeyTest {

	@Test
	public void testGetOldRowKey() {

		// Case 1: insert.
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDmlType(DMLType.INSERT);
		row0.setDatabase("puma");
		row0.setTable("test");
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap0.put("name", new ColumnInfo(false, null, "Linda"));
		row0.setColumns(columnInfoMap0);

		RowKey result0 = RowKey.getRowKey(row0);
		RowKey expected0 = new RowKey();
		expected0.setSchema("puma");
		expected0.setTable("test");
		expected0.addPriKey("id", null);

		Assert.assertEquals(expected0, result0);

		// Case 2: delete.
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setDmlType(DMLType.DELETE);
		row1.setDatabase("puma");
		row1.setTable("test");
		Map<String, ColumnInfo> columnInfoMap1 = new HashMap<String, ColumnInfo>();
		columnInfoMap1.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap1.put("name", new ColumnInfo(false, null, "Linda"));
		row1.setColumns(columnInfoMap1);

		RowKey result1 = RowKey.getRowKey(row1);
		RowKey expected1 = new RowKey();
		expected1.setSchema("puma");
		expected1.setTable("test");
		expected1.addPriKey("id", null);

		Assert.assertEquals(expected1, result1);

		// Case 3: update.
		RowChangedEvent row2 = new RowChangedEvent();
		row2.setDmlType(DMLType.UPDATE);
		row2.setDatabase("puma");
		row2.setTable("test");
		Map<String, ColumnInfo> columnInfoMap2 = new HashMap<String, ColumnInfo>();
		columnInfoMap2.put("id", new ColumnInfo(true, 0, 0));
		columnInfoMap2.put("name", new ColumnInfo(false, "James", "Linda"));
		row2.setColumns(columnInfoMap2);

		RowKey result2 = RowKey.getRowKey(row2);
		RowKey expected2 = new RowKey();
		expected2.setSchema("puma");
		expected2.setTable("test");
		expected2.addPriKey("id", 0);

		Assert.assertEquals(expected2, result2);
	}

	@Test
	public void testGetNewRowKey() {

		// Case 1: insert.
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDmlType(DMLType.INSERT);
		row0.setDatabase("puma");
		row0.setTable("test");
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap0.put("name", new ColumnInfo(false, null, "Linda"));
		row0.setColumns(columnInfoMap0);

		RowKey result0 = RowKey.getRowKey(row0);
		RowKey expected0 = new RowKey();
		expected0.setSchema("puma");
		expected0.setTable("test");
		expected0.addPriKey("id", 0);

		Assert.assertEquals(expected0, result0);

		// Case 2: delete.
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setDmlType(DMLType.DELETE);
		row1.setDatabase("puma");
		row1.setTable("test");
		Map<String, ColumnInfo> columnInfoMap1 = new HashMap<String, ColumnInfo>();
		columnInfoMap1.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap1.put("name", new ColumnInfo(false, null, "Linda"));
		row1.setColumns(columnInfoMap1);

		RowKey result1 = RowKey.getRowKey(row1);
		RowKey expected1 = new RowKey();
		expected1.setSchema("puma");
		expected1.setTable("test");
		expected1.addPriKey("id", 0);

		Assert.assertEquals(expected1, result1);

		// Case 3: update.
		RowChangedEvent row2 = new RowChangedEvent();
		row2.setDmlType(DMLType.UPDATE);
		row2.setDatabase("puma");
		row2.setTable("test");
		Map<String, ColumnInfo> columnInfoMap2 = new HashMap<String, ColumnInfo>();
		columnInfoMap2.put("id", new ColumnInfo(true, 0, 1));
		columnInfoMap2.put("name", new ColumnInfo(false, "James", "Linda"));
		row2.setColumns(columnInfoMap2);

		RowKey result2 = RowKey.getRowKey(row2);
		RowKey expected2 = new RowKey();
		expected2.setSchema("puma");
		expected2.setTable("test");
		expected2.addPriKey("id", 1);

		Assert.assertEquals(expected2, result2);
	}

	@Test
	public void test() throws PropertyVetoException, SQLException {
		final ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://192.168.224.102/");
		dataSource.setUser("root");
		dataSource.setPassword("123456");
		dataSource.setDriverClass("com.mysql.jdbc.Driver");
		dataSource.setMinPoolSize(10);
		dataSource.setMaxPoolSize(10);
		dataSource.setInitialPoolSize(10);
		dataSource.setMaxIdleTime(300);
		dataSource.setIdleConnectionTestPeriod(60);
		dataSource.setAcquireRetryAttempts(3);
		dataSource.setAcquireRetryDelay(300);
		dataSource.setMaxStatements(0);
		dataSource.setMaxStatementsPerConnection(100);
		dataSource.setNumHelperThreads(6);
		dataSource.setMaxAdministrativeTaskTime(5);
		dataSource.setPreferredTestQuery("SELECT 1");
		dataSource.setTestConnectionOnCheckin(true);

		long begin, end;

		begin = System.currentTimeMillis();
		Connection conn0 = dataSource.getConnection();
		conn0.close();
		end = System.currentTimeMillis();
		System.out.println("1:" + (end - begin));

		begin = System.currentTimeMillis();
		Connection conn1 = dataSource.getConnection();
		conn1.close();
		end = System.currentTimeMillis();
		System.out.println("2:" + (end - begin));

		begin = System.currentTimeMillis();
		Connection conn2 = dataSource.getConnection();
		conn2.close();
		end = System.currentTimeMillis();
		System.out.println("3:" + (end - begin));

		begin = System.currentTimeMillis();
		Connection conn3 = dataSource.getConnection();
		conn3.close();
		end = System.currentTimeMillis();
		System.out.println("4:" + (end - begin));

		begin = System.currentTimeMillis();
		Connection conn4 = dataSource.getConnection();
		conn4.close();
		end = System.currentTimeMillis();
		System.out.println("5:" + (end - begin));

		begin = System.currentTimeMillis();
		Connection conn5 = dataSource.getConnection();
		conn5.close();
		end = System.currentTimeMillis();
		System.out.println("6:" + (end - begin));

		begin = System.currentTimeMillis();
		Connection conn6 = dataSource.getConnection();
		conn6.close();
		end = System.currentTimeMillis();
		System.out.println("7:" + (end - begin));

		begin = System.currentTimeMillis();
		Connection conn7 = dataSource.getConnection();
		conn7.close();
		end = System.currentTimeMillis();
		System.out.println("8:" + (end - begin));
	}
}
