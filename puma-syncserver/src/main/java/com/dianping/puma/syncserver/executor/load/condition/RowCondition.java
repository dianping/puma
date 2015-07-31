package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.syncserver.common.binlog.BinlogEvent;
import com.dianping.puma.syncserver.common.binlog.DmlEvent;

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
	public boolean isLocked(BinlogEvent binlogEvent) {
		if (binlogEvent instanceof DmlEvent) {
			Row row = Row.valueOf((DmlEvent) binlogEvent);
			return rowMap.containsKey(row);
		}
		return false;
	}

	@Override
	public void lock(BinlogEvent binlogEvent) {
		if (binlogEvent instanceof DmlEvent) {
			Row row = Row.valueOf((DmlEvent) binlogEvent);
			if (rowMap.putIfAbsent(row, true) != null) {
				throw new RuntimeException("row condition lock failure.");
			}
		}
	}

	@Override
	public void unlock(BinlogEvent binlogEvent) {
		if (binlogEvent instanceof DmlEvent) {
			Row row = Row.valueOf((DmlEvent) binlogEvent);
			if (rowMap.remove(row) == null) {
				throw new RuntimeException("row condition unlock failure.");
			}
		}
	}

	protected static final class Row {

		String database;

		String table;

		Map<String, Object> pkValues = new HashMap<String, Object>();

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Row row = (Row) o;

			if (!database.equals(row.database))
				return false;
			if (!pkValues.equals(row.pkValues))
				return false;
			if (!table.equals(row.table))
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = database.hashCode();
			result = 31 * result + table.hashCode();
			result = 31 * result + pkValues.hashCode();
			return result;
		}

		public static Row valueOf(DmlEvent dmlEvent) {
			Row row = new Row();
			row.database = dmlEvent.getDatabase();
			row.table = dmlEvent.getTable();
			row.pkValues = dmlEvent.getPkValues();
			return row;
		}
	}
}
