package com.dianping.puma.taskexecutor.change;

import com.dianping.puma.core.model.TableSet;

public class TargetChangedEvent extends TaskExecutorChangedEvent {

	private TableSet tableSet;

	public TableSet getTableSet() {
		return tableSet;
	}

	public void setTableSet(TableSet tableSet) {
		this.tableSet = tableSet;
	}
}
