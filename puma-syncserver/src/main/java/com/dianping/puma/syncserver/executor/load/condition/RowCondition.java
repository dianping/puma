package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RowCondition implements Condition {

	protected ConcurrentMap<Row, Boolean> rowMap = new ConcurrentHashMap<Row, Boolean>();

	@Override
	public void reset() {
		rowMap.clear();
	}

	@Override
	public boolean isLocked(ChangedEvent binlogEvent) {
		if (binlogEvent instanceof RowChangedEvent) {
			Row row = Row.valueOf((RowChangedEvent) binlogEvent);
			return rowMap.containsKey(row);
		}

		return false;
	}

	@Override
	public void lock(ChangedEvent binlogEvent) {
		if (binlogEvent instanceof RowChangedEvent) {
			Row row = Row.valueOf((RowChangedEvent) binlogEvent);
			if (rowMap.putIfAbsent(row, true) != null) {
				throw new RuntimeException("row condition lock failure.");
			}
		}
	}

	@Override
	public void unlock(ChangedEvent binlogEvent) {
		if (binlogEvent instanceof RowChangedEvent) {
			Row row = Row.valueOf((RowChangedEvent) binlogEvent);
			if (rowMap.remove(row) == null) {
				throw new RuntimeException("row condition unlock failure.");
			}
		}
	}

	protected static final class Row {

		String database;

		String table;

		Map<String, Object> pkColumns = new HashMap<String, Object>();

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Row row = (Row) o;

			if (!database.equals(row.database))
				return false;
			if (!pkColumns.equals(row.pkColumns))
				return false;
			if (!table.equals(row.table))
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = database.hashCode();
			result = 31 * result + table.hashCode();
			result = 31 * result + pkColumns.hashCode();
			return result;
		}

		public static Row valueOf(RowChangedEvent rowChangedEvent) {
			Row row = new Row();
			row.database = rowChangedEvent.getDatabase();
			row.table = rowChangedEvent.getTable();

			DMLType dmlType = rowChangedEvent.getDmlType();
			for (Map.Entry<String, RowChangedEvent.ColumnInfo> entry: rowChangedEvent.getColumns().entrySet()) {
				if (entry.getValue().isKey()) {
					if (dmlType == DMLType.DELETE) {
						row.pkColumns.put(entry.getKey(), entry.getValue().getOldValue());
					} else {
						row.pkColumns.put(entry.getKey(), entry.getValue().getNewValue());
					}
				}
			}

			return row;
		}
	}
}
