package com.dianping.puma.admin.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlMetaInfoFetcher {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(MysqlMetaInfoFetcher.class);

	private Connection conn;

	private PreparedStatement stmt;

	public MysqlMetaInfoFetcher(String host, String username, String password) throws SQLException {
		conn = DriverManager.getConnection("jdbc:mysql://" + host + "/", username, password);
		stmt = conn.prepareStatement("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?");
	}

	public static void main(String[] args) throws SQLException {
		Connection conn = DriverManager
				.getConnection(
						"jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&noAccessToProcedureBodies=true",
						"root", "root");
		PreparedStatement stmt = conn
				.prepareStatement("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?");
		stmt.setString(1, "pumatest");
		ResultSet rs = stmt.executeQuery();
		if (rs != null) {
			while (rs.next()) {
				String tb = rs.getString("TABLE_NAME");
				System.out.println(tb);
			}
		}
	}

	/**
	 * 获取databaseConfigFrom数据库下的所有表名
	 * 
	 * @throws SQLException
	 */
	public List<String> getTables(String databaseName) throws SQLException {
		List<String> tableNames = new ArrayList<String>();
		synchronized (stmt) {
			stmt.setString(1, databaseName);
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String tb = rs.getString("TABLE_NAME");
					tableNames.add(tb);
				}
			}
		}
		return tableNames;
	}

	public void close() {
		if (stmt != null) {
			try {
				stmt.close();
				stmt = null;
			} catch (SQLException e) {
				// ignore
			}	
		}
		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				// ignore
			}
		}
	}

}
