package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.RowChangedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchRows {

	private Map<RowKey, RowChangedEvent> batchRows = new HashMap<RowKey, RowChangedEvent>();

	public void add(RowChangedEvent row) {

	}

	public void replace(RowChangedEvent oriRow, RowChangedEvent row) {

	}

	public void remove(RowChangedEvent row) {

	}

	public RowChangedEvent getOriRow(RowChangedEvent rowChangedEvent) {
		return null;
	}

	public List<RowChangedEvent> listRows() {
		return Collections.emptyList();
	}

	public int size() {
		return 0;
	}

	private class RowKey {

		private String schema;

		private String table;

		private Map<String, Object> priKeys;

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
	}
}
