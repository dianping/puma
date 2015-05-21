package com.dianping.puma.syncserver.job.load.row;

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

	/** Maximum size of batch rows, default is 1000. */
	private int maxSize = 1000;

	/** Size of batch rows. */
	private int size;

	/** `Schema`.`table` of a DML sql statement. */
	private Table table;

	/** Minimum current binlog info of batch rows. */
	private BinlogInfo binlogInfo;

	/** Minimum next binlog info of batch rows. */
	private BinlogInfo nextBinlogInfo;

	/** DDL or DML of batch rows. */
	private boolean ddl = false;

	/** Transaction commit of batch rows. */
	private boolean commit;

	/** DML type of batch rows. */
	private DMLType dmlType;

	/** Sql statement of batch rows. */
	private String sql;

	/** Sql parameters of batch rows. */
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
			if (!(event instanceof RowChangedEvent)) {
				return false;
			} else {
				RowChangedEvent row = (RowChangedEvent) event;
				if (row.isTransactionCommit()) {
					return false;
				} else {
					if (checkRow(row)) {
						params.add(LoadParser.parseArgs(row));
						rowKeys.put(RowKey.getRowKey(row), true);
						binlogInfo = new BinlogInfo(row.getBinlog(), row.getBinlogPos());
						++size;
						return true;
					} else {
						return false;
					}
				}
			}
		}
	}

	private void addFirstRow(ChangedEvent event) {
		binlogInfo = new BinlogInfo(event.getBinlog(), event.getBinlogPos());
		nextBinlogInfo = new BinlogInfo(event.getBinlog(), event.getBinlogNextPos());
		++size;

		if (event instanceof RowChangedEvent) {
			RowChangedEvent row = (RowChangedEvent) event;

			if (row.isTransactionCommit()) {
				ddl = false;
				commit = true;
			} else {
				ddl = false;
				table = new Table(row.getDatabase(), row.getTable());
				dmlType = row.getDmlType();
				sql = LoadParser.parseSql(row);
				params.add(LoadParser.parseArgs(row));
				rowKeys.put(RowKey.getRowKey(row), true);
			}
		} else {
			ddl = true;
			sql = ((DdlEvent) event).getSql();
		}
	}

	private boolean checkRow(RowChangedEvent row) {
		if (ddl) {
			return false;
		}
		if (commit) {
			return false;
		}
		if (size >= maxSize) {
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

	public boolean isCommit() {
		return commit;
	}

	public String getSql() {
		return sql;
	}

	public Object[][] getParams() {
		return params.toArray(new Object[params.size()][]);
	}
}
