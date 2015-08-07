package com.dianping.puma.syncserver.util.sql;

import com.dianping.puma.core.dto.mapping.MysqlMapping;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DDLParser;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.syncserver.exception.PumaException;

public class SqlMapper {

	public static ChangedEvent map(ChangedEvent binlogEvent, MysqlMapping mapping) {
		if (binlogEvent instanceof RowChangedEvent) {
			return mapDml((RowChangedEvent) binlogEvent, mapping);
		} else {
			return mapDdl((DdlEvent) binlogEvent, mapping);
		}
	}

	private static RowChangedEvent mapDml(RowChangedEvent rowChangedEvent, MysqlMapping mapping) {
		String oriDatabase = rowChangedEvent.getDatabase();
		String oriTable = rowChangedEvent.getTable();

		String database = mapDatabase(oriDatabase, mapping);
		String table = mapTable(oriDatabase, oriTable, mapping);

		rowChangedEvent.setDatabase(database);
		rowChangedEvent.setTable(table);

		return rowChangedEvent;
	}

	private static DdlEvent mapDdl(DdlEvent ddlEvent, MysqlMapping mapping) {
		String oriDatabase = ddlEvent.getDatabase();
		String oriTable = ddlEvent.getTable();
		String oriSql = ddlEvent.getSql();
		DDLType ddlType = ddlEvent.getDDLType();

		String database = mapDatabase(oriDatabase, mapping);
		String table = mapTable(oriDatabase, oriTable, mapping);
		String sql = mapSql(oriSql, database, table, ddlType);

		ddlEvent.setDatabase(database);
		ddlEvent.setTable(table);
		ddlEvent.setSql(sql);

		return ddlEvent;
	}

	private static String mapDatabase(String oriDatabase, MysqlMapping mapping) {
		if (oriDatabase == null) {
			throw new PumaException("map binlog event failure, no original database.");
		}

		String database = mapping.getDatabase(oriDatabase);
		if (database == null) {
			throw new PumaException("map binlog event failure, no mapping database.");
		}
		return database;
	}

	private static String mapTable(String oriDatabase, String oriTable, MysqlMapping mapping) {
		if (oriDatabase == null || oriTable == null) {
			throw new PumaException("map binlog event failure, no original database or table.");
		}

		String table = mapping.getTable(oriDatabase, oriTable);
		if (table == null) {
			throw new PumaException("map binlog event failure, no mapping table.");
		}
		return table;
	}

	private static String mapSql(String oriSql, String database, String table, DDLType ddlType) {
		if (oriSql == null) {
			throw new PumaException("map binlog event failure, no original sql.");
		}

		String sql = DDLParser.replaceDdl(oriSql, database, table, ddlType);
		if (sql == null) {
			throw new PumaException("map binlog event failure, no mapping sql.");
		}
		return sql;
	}
}
