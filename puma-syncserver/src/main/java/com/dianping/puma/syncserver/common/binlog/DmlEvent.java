package com.dianping.puma.syncserver.common.binlog;

import java.util.HashMap;
import java.util.Map;

public class DmlEvent extends BinlogEvent {

	private Map<String, Column> columns;

	public Map<String, Object> getPkValues() {
		Map<String, Object> pkValues = new HashMap<String, Object>();

		for (Map.Entry<String, Column> entry: columns.entrySet()) {
			String columnName = entry.getKey();
			Column column = entry.getValue();

			if (column.isPk()) {
				if (getEventType().equals(EventType.DELETE)) {
					pkValues.put(columnName, column.getOldValue());
				} else {
					pkValues.put(columnName, column.getNewValue());
				}
			}
		}

		return pkValues;
	}

	public void addColumn(String columnName, Column column) {
		if (columns == null) {
			columns = new HashMap<String, Column>();
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
