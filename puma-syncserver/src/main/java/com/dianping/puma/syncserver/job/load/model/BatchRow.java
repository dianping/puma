package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.LoadParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchRow {

	private final static int volume = 1000;

	private int size;

	private BinlogInfo binlogInfo;

	private Table table;

	private boolean ddl = false;

	private DMLType dmlType;

	private boolean isTransactionBegin;

	private boolean isTransactionCommit;

	private String sql;

	private List<Object[]> params = new ArrayList<Object[]>();

	private Map<RowKey, Boolean> rowKeys = new HashMap<RowKey, Boolean>();

	public BatchRow() {
	}

	public BatchRow(ChangedEvent row) {
		addFirstRow(row);
	}

	public boolean addRow(ChangedEvent event) {
		if (size == 0) {
			addFirstRow(event);
			return true;
		} else {
			if (event instanceof RowChangedEvent) {
				RowChangedEvent row = (RowChangedEvent) event;
				if (checkRow(row)) {
					params.add(LoadParser.parseArgs(row));
					rowKeys.put(RowKey.getRowKey(row), true);
					binlogInfo = new BinlogInfo(row.getBinlog(), row.getBinlogPos());
					++size;
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	private void addFirstRow(ChangedEvent event) {
		binlogInfo = new BinlogInfo(event.getBinlog(), event.getBinlogPos());
		++size;

		if (event instanceof RowChangedEvent) {
			RowChangedEvent row = (RowChangedEvent) event;
			ddl = false;
			table = new Table(row.getDatabase(), row.getTable());
			dmlType = row.getDmlType();
			sql = LoadParser.parseSql(row);
			params.add(LoadParser.parseArgs(row));
			rowKeys.put(RowKey.getRowKey(row), true);
		} else {
			ddl = true;
			sql = ((DdlEvent) event).getSql();
		}
	}

	private boolean checkRow(RowChangedEvent row) {
		if (ddl) {
			return false;
		}
		if (size >= volume) {
			return false;
		}
		if (!this.table.equals(new Table(row.getDatabase(), row.getTable()))) {
			return false;
		}
		if (row.getDmlType() != dmlType) {
			return false;
		}
		if (rowKeys.containsKey(RowKey.getRowKey(row))) {
			return false;
		}
		return true;
	}

	public int size() {
		return size;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public Map<RowKey, Boolean> getRowKeys() {
		return rowKeys;
	}

	public boolean isDdl() {
		return ddl;
	}

	public boolean isTransactionBegin() {
		return isTransactionBegin;
	}

	public boolean isTransactionCommit() {
		return isTransactionCommit;
	}

	public String getSql() {
		return sql;
	}

	public Object[][] getParams() {
		return params.toArray(new Object[params.size()][]);
	}
}
