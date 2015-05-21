package com.dianping.puma.syncserver.job.load.row;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;

import java.util.HashMap;
import java.util.Map;

public class RowKey {

	private String schema;

	private String table;

	private Map<String, Object> priKeys = new HashMap<String, Object>();

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

	public void addPriKey(String name, Object value) {
		// Primary key value should not be NULL.
		if (value != null) {
			priKeys.put(name, value);
		}
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

	public static boolean equals(RowChangedEvent aRow, RowChangedEvent bRow) {
		return RowKey.getRowKey(aRow).equals(RowKey.getRowKey(bRow));
	}

	public static RowKey getRowKey(RowChangedEvent row) {
		switch (row.getDmlType()) {
		case INSERT:
			return getNewRowKey(row);
		case DELETE:
			return getOldRowKey(row);
		case UPDATE:
		case REPLACE:
			return getNewRowKey(row);
		}

		return null;
	}

	private static RowKey getNewRowKey(RowChangedEvent row) {
		RowKey rowKey = new RowKey();

		rowKey.setSchema(row.getDatabase());
		rowKey.setTable(row.getTable());
		for (Map.Entry<String, ColumnInfo> entry: row.getColumns().entrySet()) {
			if (entry.getValue().isKey()) {
				rowKey.addPriKey(entry.getKey(), entry.getValue().getNewValue());
			}
		}

		return rowKey;
	}

	private static RowKey getOldRowKey(RowChangedEvent row) {
		RowKey oriRowKey = new RowKey();

		oriRowKey.setSchema(row.getDatabase());
		oriRowKey.setTable(row.getTable());
		for (Map.Entry<String, ColumnInfo> entry: row.getColumns().entrySet()) {
			if (entry.getValue().isKey()) {
				oriRowKey.addPriKey(entry.getKey(), entry.getValue().getOldValue());
			}
		}

		return oriRowKey;
	}
}

