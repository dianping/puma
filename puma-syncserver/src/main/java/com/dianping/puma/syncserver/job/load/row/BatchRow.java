package com.dianping.puma.syncserver.job.load.row;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.LoadParser;

import java.util.*;

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

	private long seq;

	/** Execution time of a random row in batch rows. */
	private long executeTime;

	/** DDL or DML of batch rows. */
	private boolean ddl = false;

	/** Transaction commit of batch rows. */
	private boolean commit;

	/** DML type of batch rows. */
	private DMLType dmlType;

	private String sql;

	private List<Object[]> params = new ArrayList<Object[]>();

	private String u2iSql;

	private List<Object[]> u2iParams = new ArrayList<Object[]>();

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
				RowChangedEvent u2iRow = row.clone();
				u2iRow.setDmlType(DMLType.REPLACE);

				if (row.isTransactionCommit()) {
					return false;
				} else {
					if (checkRow(row)) {
						params.add(LoadParser.parseArgs(row));
						u2iParams.add(LoadParser.parseArgs(u2iRow));
						rowKeys.put(RowKey.getRowKey(row), true);
						executeTime = row.getExecuteTime();
						binlogInfo = new BinlogInfo(row.getBinlogInfo().getBinlogFile(), row.getBinlogInfo()
								.getBinlogPosition());
						seq = row.getSeq();
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
		executeTime = event.getExecuteTime();
		binlogInfo = new BinlogInfo(event.getBinlogInfo().getBinlogFile(), event.getBinlogInfo()
				.getBinlogPosition());
		seq = event.getSeq();
		++size;

		if (event instanceof RowChangedEvent) {
			RowChangedEvent row = (RowChangedEvent) event;
			RowChangedEvent u2iRow = row.clone();
			u2iRow.setDmlType(DMLType.INSERT);

			if (row.isTransactionCommit()) {
				ddl = false;
				commit = true;
			} else {
				ddl = false;
				table = new Table(row.getDatabase(), row.getTable());
				dmlType = row.getDmlType();
				sql = LoadParser.parseSql(row);
				u2iSql = LoadParser.parseSql(u2iRow);
				params.add(LoadParser.parseArgs(row));
				u2iParams.add(LoadParser.parseArgs(u2iRow));
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

	@Override
	public String toString() {
		return "BatchRow{" + "size=" + size + ", table=" + table + ", binlogInfo=" + binlogInfo + ", seq=" + seq
				+ ", ddl=" + ddl + ", commit=" + commit + ", dmlType=" + dmlType + ", sql='" + sql + '\'' + ", params="
				+ Arrays.deepToString(getParams()) + '}';
	}

	public int size() {
		return size;
	}

	public long getExecuteTime() {
		return executeTime;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public long getSeq() {
		return seq;
	}

	public Map<RowKey, Boolean> getRowKeys() {
		return rowKeys;
	}

	public boolean isDdl() {
		return ddl;
	}

	public DMLType getDmlType() {
		return dmlType;
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

	public String getU2iSql() {
		return u2iSql;
	}

	public Object[][] getU2iParams() {
		return u2iParams.toArray(new Object[u2iParams.size()][]);
	}
}
