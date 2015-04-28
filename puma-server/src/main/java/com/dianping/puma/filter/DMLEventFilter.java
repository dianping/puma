package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.core.model.event.AcceptedTableChangedEvent;
import com.dianping.puma.core.model.event.EventListener;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMLEventFilter extends AbstractEventFilter implements EventListener<AcceptedTableChangedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(DMLEventFilter.class);

	private String name;

	private boolean dml = true;

	private TableSet acceptedTables = new TableSet();

	public void init(boolean dml) {
		this.dml = dml;
	}

	protected boolean checkEvent(ChangedEvent changedEvent) {
		if (changedEvent == null) {
			return false;
		}

		if (changedEvent instanceof RowChangedEvent) {

			// Transaction or not.
			if (((RowChangedEvent) changedEvent).isTransactionBegin() || ((RowChangedEvent) changedEvent)
					.isTransactionCommit()) {
				return true;
			}

			// Need dml or not.
			if (!dml) {
				return false;
			}

			// In accepted table list.
			Table table = new Table(changedEvent.getDatabase(), changedEvent.getTable());
			if (!acceptedTables.contains(table)) {
				return false;
			}

			return true;
		}

		return true;
	}

	@Subscribe
	public void onEvent(AcceptedTableChangedEvent event) {
		if (event.getName().equals(name)) {
			LOG.info("`DMLEventFilter` receives event: {}.", event.toString());

			TableSet tableSet = event.getTableSet();
			addAcceptedTables(tableSet);
		}
	}

	public void addAcceptedTables(TableSet acceptedTables) {
		this.acceptedTables = acceptedTables;
	}

	public void setName(String name) {
		this.name = name;
	}
}
