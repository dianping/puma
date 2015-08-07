package com.dianping.puma.syncserver.common.binlog;

import com.dianping.puma.syncserver.util.mysql.MySqlTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertEvent extends DmlEvent {

	@Override
	public Map<String, Object> buildPkValues() {
		Map<String, Object> pkValues = new HashMap<String, Object>();

		for (Map.Entry<String, Column> entry: columns.entrySet()) {
			String columnName = entry.getKey();
			Column column = entry.getValue();
			if (column.isPk()) {
				pkValues.put(columnName, column.getNewValue());
			}
		}

		return pkValues;
	}

	@Override
	public void buildSql() {
		sql = MySqlTemplate.render(database, table, columns, MySqlTemplate.RenderTemplate.INSERT);
	}

	@Override
	public void buildParams() {
		List<Object> paramList = new ArrayList<Object>();

		for (Map.Entry<String, Column> entry: columns.entrySet()) {
			Column column = entry.getValue();
			paramList.add(column.getNewValue());
		}

		params = paramList.toArray();
	}
}
