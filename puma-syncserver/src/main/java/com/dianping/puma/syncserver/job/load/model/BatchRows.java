package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.RowChangedEvent;

import java.util.*;

public class BatchRows {

	private Map<RowKey, RowChangedEvent> batchRows = new HashMap<RowKey, RowChangedEvent>();

	public void add(RowKey rowKey, RowChangedEvent row) {
		batchRows.put(rowKey, row);
	}

	public void replace(RowKey rowKey, RowChangedEvent row) {
		batchRows.put(rowKey, row);
	}

	public void remove(RowKey rowKey) {
		batchRows.remove(rowKey);
	}

	public void clear() {
		batchRows.clear();
	}

	public boolean contain(RowKey rowKey) {
		return batchRows.containsKey(rowKey);
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
