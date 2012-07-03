package com.dianping.puma.sender.filter;

import com.dianping.puma.client.ChangedEvent;
import com.dianping.puma.client.DdlEvent;
import com.dianping.puma.client.RowChangedEvent;

public class DmlDdlEventFilter extends AbstractEventFilter {

	private int	operationType;

	public void setOperationType(String operationType) {
		if (operationType.trim().toLowerCase().equals("dml")) {
			this.operationType = 0;
		} else if (operationType.trim().toLowerCase().equals("ddl")) {
			this.operationType = 1;
		} else if (operationType.trim().toLowerCase().equals("both")) {
			this.operationType = 2;
		}
	}

	protected boolean checkEvent(ChangedEvent event) {
		switch (this.operationType) {
		// dml_only
			case 0:
				if (event instanceof RowChangedEvent) {
					return true;
				}
				break;
			// ddl_only
			case 1:
				if (event instanceof DdlEvent) {
					return true;
				}
				break;
			// both
			case 2:
				return true;
			default:
				break;
		}
		return false;
	}
}