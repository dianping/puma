package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;

import java.util.HashMap;
import java.util.Map;

public class RowKey {

	private String schema;

	private String table;

	private Map<String, Object> priKeys;

	public RowKey() {}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Map<String, Object> getPriKeys() {
		return priKeys;
	}

	public void setPriKeys(Map<String, Object> priKeys) {
		this.priKeys = priKeys;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RowKey rowKey = (RowKey) o;

		if (!priKeys.equals(rowKey.priKeys))
			return false;
		if (!schema.equals(rowKey.schema))
			return false;
		if (!table.equals(rowKey.table))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = schema.hashCode();
		result = 31 * result + table.hashCode();
		result = 31 * result + priKeys.hashCode();
		return result;
	}

	public static RowKey getNewRowKey(RowChangedEvent row) {
		RowKey rowKey = new RowKey();

		rowKey.setSchema(row.getDatabase());
		rowKey.setTable(row.getTable());
		Map<String, Object> priKeys = new HashMap<String, Object>();
		for (Map.Entry<String, ColumnInfo> entry: row.getColumns().entrySet()) {
			if (entry.getValue().isKey()) {
				priKeys.put(entry.getKey(), entry.getValue().getNewValue());
			}
		}
		rowKey.setPriKeys(priKeys);

		return rowKey;
	}

	public static RowKey getOldRowKey(RowChangedEvent row) {
		RowKey oriRowKey = new RowKey();

		oriRowKey.setSchema(row.getDatabase());
		oriRowKey.setTable(row.getTable());
		Map<String, Object> priKeys = new HashMap<String, Object>();
		for (Map.Entry<String, ColumnInfo> entry: row.getColumns().entrySet()) {
			if (entry.getValue().isKey()) {
				priKeys.put(entry.getKey(), entry.getValue().getOldValue());
			}
		}
		oriRowKey.setPriKeys(priKeys);

		return oriRowKey;
	}
}
