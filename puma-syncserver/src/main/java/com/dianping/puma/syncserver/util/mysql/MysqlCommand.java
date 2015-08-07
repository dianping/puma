package com.dianping.puma.syncserver.util.mysql;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;

public class MysqlCommand {

	private String host;

	private int port;

	private String username;

	private String password;

	private QueryRunner queryRunner;

	public MysqlCommand(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;

		HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port);
		hikariDataSource.setDriverClassName("com.mysql.jdbc.Driver");
		hikariDataSource.setUsername(username);
		hikariDataSource.setPassword(password);
		hikariDataSource.setMaximumPoolSize(5);
		hikariDataSource.setAutoCommit(true);
		hikariDataSource.setConnectionTestQuery("select 1");

		queryRunner = new QueryRunner(hikariDataSource);
	}

	public void dropDatabase(String database) throws SQLException {
		queryRunner.update(String.format("drop database if exists `%s`", database));
	}

	public void createDatabase(String database) throws SQLException {
		queryRunner.update(String.format("create database if not exists `%s`", database));
	}

	public void dropTable(String database, String table) throws SQLException {
		queryRunner.update(String.format("drop table if exists `%s`.`%s`", database, table));
	}

	public void createTable(String database, String table) throws SQLException {
		queryRunner.update(
				String.format(
						"create table if not exists `%s`.`%s` (`id` int(11) unsigned NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`))",
						database, table
				)
		);
	}

	public void addColumn(String database, String table, String column, String mysqlType) throws SQLException {
		queryRunner.update(
				String.format(
						"ALTER TABLE `%s`.`%s` ADD COLUMN `%s` %s",
						database, table, column, mysqlType
				)
		);
	}
}
