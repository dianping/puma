package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.syncserver.common.binlog.*;
import com.dianping.puma.syncserver.util.mysql.MysqlCommand;
import com.dianping.puma.syncserver.util.mysql.MysqlRandom;
import com.dianping.zebra.shard.parser.sqlParser.Insert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AsyncLoaderTest {

	private String host = "127.0.0.1";
	private int port = 3306;
	private String username = "root";
	private String password = "123456";

	private String database = "puma-db";
	private String table = "puma-tb";

	private MysqlCommand mysqlCommand;

	private int n = 100000;

	@Before
	public void before() throws SQLException {
		mysqlCommand = new MysqlCommand(host, port, username, password);
		mysqlCommand.dropDatabase(database);
		mysqlCommand.createDatabase(database);
		mysqlCommand.createTable(database, table);
	}

	@Test
	// sql.Types == "VARCHAR"
	public void testVarchar() throws SQLException {
		/*
		mysqlCommand.addColumn(database, table, "c-varchar", "varchar(20)");

		Map<Integer, String> expected = new HashMap<Integer, String>();
		for (int i = 0; i != n; ++i) {
			DmlEvent dmlEvent = randomDmlEvent(database, table);
			addRandomIdColumn(dmlEvent, 0, 1000);
			addRandomVarcharColumn(dmlEvent, "c-varchar", false);

			if (expected)
		}*/

	}

	private DmlEvent randomDmlEvent(String database, String table) {
		DmlEvent event = null;

		int type = MysqlRandom.randomInteger(0, 2);
		switch (type) {
		case 0:
			event = new InsertEvent();
			break;
		case 1:
			event = new DeleteEvent();
			break;
		case 2:
			event = new UpdateEvent();
			break;
		}

		event.setDatabase(database);
		event.setTable(table);
		return event;
	}

	private void addRandomIdColumn(DmlEvent dmlEvent, int min, int max) {
		Integer id = MysqlRandom.randomInteger(min, max);
		String column = "id";

		if (dmlEvent instanceof InsertEvent) {
			dmlEvent.addColumn(column, new Column(true, null, id));
		} else if (dmlEvent instanceof DeleteEvent) {
			dmlEvent.addColumn(column, new Column(true, id, null));
		} else if (dmlEvent instanceof UpdateEvent) {
			dmlEvent.addColumn(column, new Column(true, id, id));
		}
	}

	private void addRandomVarcharColumn(DmlEvent dmlEvent, String column, boolean pk) {
		String oldVarchar = MysqlRandom.randomVarchar(10);
		String newVarchar = MysqlRandom.randomVarchar(10);

		if (dmlEvent instanceof InsertEvent) {
			dmlEvent.addColumn(column, new Column(pk, null, newVarchar));
		} else if (dmlEvent instanceof DeleteEvent) {
			dmlEvent.addColumn(column, new Column(pk, oldVarchar, null));
		} else if (dmlEvent instanceof UpdateEvent) {
			dmlEvent.addColumn(column, new Column(pk, oldVarchar, newVarchar));
		}
	}
}