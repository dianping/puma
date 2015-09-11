package com.dianping.puma.syncserver.common.binlog;

import com.dianping.puma.syncserver.executor.transform.rule.DataSourceMappingRule;
import com.dianping.puma.syncserver.executor.transform.rule.DbTbMappingRule;
import org.apache.commons.lang3.tuple.Pair;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class DmlEvent extends BinlogEvent {

	protected Map<String, Column> columns;

	@Override
	public DataSource mapDataSource(DataSourceMappingRule rule) {
		return rule.map(database, table, columns);
	}

	@Override
	public Pair<String, String> mapDbTb(DbTbMappingRule rule) {
		return rule.map(database, table, columns);
	}

	public abstract Map<String, Object> buildPkValues();

	public void addColumn(String columnName, Column column) {
		if (columns == null) {
			columns = new LinkedHashMap<String, Column>();
		}

		columns.put(columnName, column);
	}

	public Map<String, Column> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, Column> columns) {
		this.columns = columns;
	}
}
