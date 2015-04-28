package com.dianping.puma.core.model.event;

import com.dianping.puma.core.model.TableSet;

public class AcceptedTableChangedEvent extends Event {

	private TableSet tableSet;

	public TableSet getTableSet() {
		return tableSet;
	}

	public void setTableSet(TableSet tableSet) {
		this.tableSet = tableSet;
	}

	@Override
	public String toString() {
		return "AcceptedTableChangedEvent{" +
				"schemaTableSet=" + tableSet +
				'}';
	}
}
