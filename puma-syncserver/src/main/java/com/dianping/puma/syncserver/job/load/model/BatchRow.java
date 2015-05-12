package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.LoadParser;

import java.util.ArrayList;
import java.util.List;

public class BatchRow {

	private int batchRowMaxSize = 1000;

	private int batchRowSize;

	private BinlogInfo binlogInfo;

	private Table table;

	private boolean ddl;

	private DMLType dmlType;

	private String sql;

	private List<Object[]> params = new ArrayList<Object[]>();

	private List<RowKey> rowKeys = new ArrayList<RowKey>();

	public BatchRow() {}

	public BatchRow(ChangedEvent row) {
		init(row);
	}

	private void init(ChangedEvent row) {
		binlogInfo = new BinlogInfo(row.getBinlog(), row.getBinlogPos());
		++batchRowSize;

		if (row instanceof RowChangedEvent) {
			ddl = false;
			table = new Table(row.getDatabase(), row.getTable());
			dmlType = ((RowChangedEvent) row).getDmlType();
			sql = LoadParser.parseSql((RowChangedEvent) row);
			params.add(LoadParser.parseArgs((RowChangedEvent) row));
			rowKeys.add(RowKey.getRowKey((RowChangedEvent) row));
		} else {
			ddl = true;
			sql = ((DdlEvent) row).getSql();
		}
	}

	public boolean addRow(ChangedEvent row) {
		if (batchRowSize == 0) {
			init(row);
			return true;
		} else {
			if (row instanceof RowChangedEvent) {
				if (check((RowChangedEvent) row)) {
					params.add(LoadParser.parseArgs((RowChangedEvent) row));
					rowKeys.add(RowKey.getRowKey((RowChangedEvent) row));
					++batchRowSize;
					return true;
				}
				return false;
			}
			return false;
		}
	}

	private boolean check(RowChangedEvent row) {
		Table table = new Table(row.getDatabase(), row.getTable());
		return this.table.equals(table) && row.getDmlType() == dmlType && batchRowSize <= batchRowMaxSize;
	}

	public List<RowKey> listRowKeys() {
		return rowKeys;
	}

	public boolean isDdl() {
		return ddl;
	}

	public String getSql() {
		return sql;
	}

	public Object[][] getParams() {
		return params.toArray(new Object[params.size()][]);
	}
}
