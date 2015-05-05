package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.RowChangedEvent;

import java.util.*;

public class BatchRows {

	private Map<RowKey, RowChangedEvent> batchRows = new HashMap<RowKey, RowChangedEvent>();

	public void add(RowChangedEvent row) {
		switch (row.getDMLType()) {
		case INSERT:
			batchRows.put(RowKey.getNewRowKey(row), row);
			break;
		case DELETE:
			batchRows.put(RowKey.getOldRowKey(row), row);
			break;
		case UPDATE:
			batchRows.put(RowKey.getNewRowKey(row), row);
			break;
		}
	}

	public void replace(RowChangedEvent row) {
		add(row);
	}

	public void remove(RowChangedEvent row) {
		RowKey rowKey = null;

		switch (row.getDMLType()) {
		case INSERT:
			rowKey = RowKey.getNewRowKey(row);
			break;
		case DELETE:
			rowKey = RowKey.getOldRowKey(row);
			break;
		case UPDATE:
			rowKey = RowKey.getNewRowKey(row);
			break;
		}

		batchRows.remove(rowKey);
	}

	public void clear() {
		batchRows.clear();
	}

	public RowChangedEvent get(RowKey rowKey) {
		return batchRows.get(rowKey);
	}

	public List<RowChangedEvent> listRows() {
		return new ArrayList<RowChangedEvent>(batchRows.values());
	}

	public int size() {
		return batchRows.size();
	}
}
