package com.dianping.puma.admin.db;

import com.dianping.puma.core.config.ConfigManager;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CommandTableService implements TableService {

	private final long timeout = 60 * 1000; // 60s.

	@Autowired
	ConfigManager configManager;

	@Autowired
	DatabaseService databaseService;

	private ConcurrentMap<String, List<String>> tableCache = new ConcurrentHashMap<String, List<String>>();

	private ConcurrentMap<String, Date> tableCacheTimes = new ConcurrentHashMap<String, Date>();

	@Override
	public List<String> getTables(String database) {
		List<String> tables = tableCache.get(database);
		if (tables != null && !isTimeout(database)) {
			return tables;
		} else {
			try {
				tables = getTablesWithoutCache(database);
				tableCache.put(database, tables);
				tableCacheTimes.put(database, new Date());
				return tables;
			} catch (SQLException e) {
				return Lists.newArrayList();
			}
		}
	}

	protected List<String> getTablesWithoutCache(String database) throws SQLException {
		return newQueryRunner(database).query("show tables", new ResultSetHandler<List<String>>() {
			@Override
			public List<String> handle(ResultSet rs) throws SQLException {
				List<String> result = new ArrayList<String>();
				while (rs.next()) {
					String table = rs.getString(1);
					result.add(table);
				}
				return result;
			}
		});
	}

	protected QueryRunner newQueryRunner(String database) {
		HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setJdbcUrl(databaseService.findWriteJdbcUrl(database));
		hikariDataSource.setDriverClassName("com.mysql.jdbc.Driver");
		hikariDataSource.setUsername(configManager.getConfig("puma.server.binlog.username"));
		hikariDataSource.setPassword(configManager.getConfig("puma.server.binlog.password"));
		hikariDataSource.setMaximumPoolSize(5);
		hikariDataSource.setAutoCommit(true);
		hikariDataSource.setConnectionTestQuery("select 1");
		return new QueryRunner(hikariDataSource);
	}

	protected boolean isTimeout(String jdbcUrl) {
		Date date = tableCacheTimes.get(jdbcUrl);
		return date == null || new Date().getTime() - date.getTime() > timeout;
	}
}
