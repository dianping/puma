package com.dianping.puma.syncserver.mysqldump;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

public class MysqlDumpPressureTest extends TestCase {
	private Connection conn;

	private Statement stmt;

	public void before(String host, String username, String password) throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn = DriverManager.getConnection("jdbc:mysql://" + host + "/puma_test1", username, password);

	}

	public void insert(int maxCount) {
		try {
			int index = 4;
			while (index < maxCount) {
				stmt = conn.createStatement();
				String sql = String.format("insert into business(business.name,business.sex)values('%s','%s')",
						"business" + Integer.toString(index), index % 2 == 0 ? "M" : "F");
				stmt.execute(sql);
				index++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {

				}
				stmt = null;
			}
		}

	}

	public void after() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {

			}
			conn = null;
		}
	}

	public static void main(String[] args) {
		MysqlDumpPressureTest test = new MysqlDumpPressureTest();
		try {
			test.before("192.168.224.101:3306", "root", "123456");
			test.insert(5);
			test.after();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
