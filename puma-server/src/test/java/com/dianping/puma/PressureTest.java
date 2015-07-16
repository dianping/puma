package com.dianping.puma;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public class PressureTest {


	ComboPooledDataSource dataSource;

	Connection conn;

	QueryRunner queryRunner;

	public static void main(String[] args) throws SQLException {
		PressureTest test = new PressureTest();
		test.init();
		//test.initSQL();
		//test.insertSQL();
		test.updateSQL();

		System.out.println("Finished.");
	}

	private void init() {
		String host = "192.168.224.101:3306"; // beta on puma server.
		String username = "root";
		String password = "123456";

		try {
			dataSource = new ComboPooledDataSource();
			dataSource.setJdbcUrl("jdbc:mysql://" + host + "/");
			dataSource.setUser(username);
			dataSource.setPassword(password);
			dataSource.setDriverClass("com.mysql.jdbc.Driver");
			queryRunner = new QueryRunner(dataSource);
		} catch (PropertyVetoException e) {
			throw new RuntimeException();
		}
	}

//	private void initSQL() throws SQLException {
//		try {
//			conn = dataSource.getConnection();
//
//			// Drop database.
//			//String dropSQL = "DROP SCHEMA IF EXISTS Pressure;";
//			//queryRunner.update(dropSQL);
//
//			// Create database.
//			String createSQL = "CREATE SCHEMA IF NOT EXISTS Pressure;";
//			queryRunner.update(createSQL);
//
//			// Create tables.
//			String createTableSQL0 = "CREATE TABLE IF NOT EXISTS `Pressure`.`user` (\n"
//					+ "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n"
//					+ "  `name` varchar(20) DEFAULT NULL,\n"
//					+ "  PRIMARY KEY (`id`)\n"
//					+ ") ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;";
//
//			queryRunner.update(createTableSQL0);
//
//			String createTableSQL1 = "CREATE TABLE IF NOT EXISTS `Pressure`.`business` (\n"
//					+ "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n"
//					+ "  `name` varchar(20) DEFAULT NULL,\n"
//					+ "  PRIMARY KEY (`id`)\n"
//					+ ") ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;";
//			queryRunner.update(createTableSQL1);
//
//		} finally {
//			DbUtils.close(conn);
//		}
//	}

//	private void insertSQL() throws SQLException {
//		try {
//			conn = dataSource.getConnection();
//			String insertSQL0 = "INSERT INTO Pressure.user (name) VALUES (?)";
//
//			for (int i = 0; i != 1000000; ++i) {
//				queryRunner.update(insertSQL0, "aaa");
//			}
//
//		} finally {
//			DbUtils.close(conn);
//		}
//	}

	private void updateSQL() throws SQLException {
		try {
			conn = dataSource.getConnection();
			String updateSQL = "UPDATE Pressure.user SET name = ? WHERE name = ?";

			queryRunner.update(updateSQL, "bbb", "aaa");
			queryRunner.update(updateSQL, "ddd", "bbb");
			queryRunner.update(updateSQL, "eee", "ddd");
			queryRunner.update(updateSQL, "fff", "eee");
		} finally {
			DbUtils.close(conn);
		}
	}
}
