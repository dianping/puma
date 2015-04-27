package com.dianping.puma;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.RandomStringUtils;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public class CoverageTest {

	ComboPooledDataSource dataSource;

	Connection conn;

	QueryRunner queryRunner;

	public static void main(String[] args) throws SQLException {
		CoverageTest test = new CoverageTest();
		test.init();
		test.initSQL();
		test.insertSQL();

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

	private void initSQL() throws SQLException {
		try {
			conn = dataSource.getConnection();

			// Drop database.
			String dropSQL = "DROP SCHEMA IF EXISTS Coverage;";
			queryRunner.update(dropSQL);

			// Create database.
			String createSQL = "CREATE SCHEMA IF NOT EXISTS Coverage;";
			queryRunner.update(createSQL);

			// Create tables.
			String createTableSQL0 = "CREATE TABLE `Coverage`.`user` (\n"
					+ "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n"
					+ "  `name` varchar(20) DEFAULT NULL,\n"
					+ "  PRIMARY KEY (`id`)\n"
					+ ") ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;";

			queryRunner.update(createTableSQL0);

			String createTableSQL1 = "CREATE TABLE `Coverage`.`business` (\n"
					+ "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n"
					+ "  `name` varchar(20) DEFAULT NULL,\n"
					+ "  PRIMARY KEY (`id`)\n"
					+ ") ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;";
			queryRunner.update(createTableSQL1);

		} finally {
			DbUtils.close(conn);
		}
	}

	private void insertSQL() throws SQLException {
		try {
			conn = dataSource.getConnection();
			String insertSQL0 = "INSERT INTO Coverage.user (name) VALUES (?)";

			for (int i = 0; i != 10000; ++i) {
				queryRunner.update(insertSQL0, RandomStringUtils.random(10, true, false));
			}

		} finally {
			DbUtils.close(conn);
		}
	}
}
