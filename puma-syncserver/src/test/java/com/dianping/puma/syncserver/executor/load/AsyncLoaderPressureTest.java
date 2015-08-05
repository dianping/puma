package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.syncserver.common.binlog.Column;
import com.dianping.puma.syncserver.common.binlog.InsertEvent;
import com.dianping.puma.syncserver.common.binlog.ReplaceEvent;
import com.dianping.puma.syncserver.executor.transform.RuleBasedTransformer;
import com.dianping.puma.syncserver.executor.transform.Transformer;
import com.dianping.puma.syncserver.executor.transform.rule.DataSourceMappingRule;
import com.dianping.puma.syncserver.executor.transform.rule.DbTbMappingRule;
import com.dianping.puma.syncserver.util.mysql.MysqlCommand;
import com.dianping.puma.syncserver.util.mysql.MysqlRandom;
import com.google.common.base.Stopwatch;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncLoaderPressureTest {

	public static void main(String[] args) throws SQLException {
		String host = "127.0.0.1";
		int port = 3306;
		String username = "root";
		String password = "123456";

		String database = "async-loader-db";
		String table = "async-loader-tb";

		// Create a new table.
		MysqlCommand mysqlCommand = new MysqlCommand(host, port, username, password);
		mysqlCommand.dropDatabase(database);
		mysqlCommand.createDatabase(database);
		mysqlCommand.createTable(database, table);

		// Create a new column, type `varchar`;
		mysqlCommand.addColumn(database, table, "CVarchar", "varchar(20)");

		// Datasource.
		final HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port);
		hikariDataSource.setDriverClassName("com.mysql.jdbc.Driver");
		hikariDataSource.setUsername(username);
		hikariDataSource.setPassword(password);
		hikariDataSource.setMaximumPoolSize(5);
		hikariDataSource.setAutoCommit(true);
		hikariDataSource.setConnectionTestQuery("select 1");

		// Transformer.
		RuleBasedTransformer transformer = new RuleBasedTransformer();
		transformer.setDataSourceMappingRule(new DataSourceMappingRule() {
			@Override
			public DataSource map(String database, String table, Map<String, Column> columns) {
				return hikariDataSource;
			}
		});
		transformer.setDbTbMappingRule(new DbTbMappingRule() {
			@Override
			public Pair<String, String> map(String database, String table, Map<String, Column> columns) {
				return Pair.of(database, table);
			}
		});

		// Loader.
		AsyncLoader asyncLoader = new AsyncLoader(1, Executors.newCachedThreadPool());
		asyncLoader.start();

		int n = 10000;

		Stopwatch stopwatch = Stopwatch.createStarted();
		for (int i = 0; i != n; ++i) {
			Integer id = MysqlRandom.randomInteger(5);
			String varchar = MysqlRandom.randomVarchar(10);

			ReplaceEvent replaceEvent = new ReplaceEvent();
			replaceEvent.setDatabase(database);
			replaceEvent.setTable(table);
			replaceEvent.addColumn("id", new Column(true, null, id));
			replaceEvent.addColumn("CVarchar", new Column(false, null, varchar));

			transformer.transform(replaceEvent);
			asyncLoader.load(replaceEvent).addListener(new LoadFutureListener() {
				@Override public void onSuccess(Integer result) {

				}

				@Override public void onFailure(Throwable cause) {
					System.out.println("failure:" + cause);
				}
			});
		}
		stopwatch.stop();

		System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
	}
}
