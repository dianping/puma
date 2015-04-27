package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.SchemaTable;
import com.dianping.puma.core.model.SchemaTableSet;
import com.dianping.puma.core.model.event.AcceptedTableChangedEvent;
import com.dianping.puma.core.model.event.EventListener;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMLEventFilter extends AbstractEventFilter implements EventListener<AcceptedTableChangedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(DMLEventFilter.class);

	private String name;

	private boolean dml = true;

	private SchemaTableSet acceptedTables = new SchemaTableSet();

	public void init(boolean dml) {
		this.dml = dml;
	}

	protected boolean checkEvent(ChangedEvent changedEvent) {
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
			SchemaTable schemaTable = new SchemaTable(changedEvent.getDatabase(), changedEvent.getTable());
			if (!acceptedTables.contains(schemaTable)) {
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

			SchemaTableSet schemaTableSet = event.getSchemaTableSet();
			addAcceptedTables(schemaTableSet);
		}
	}

	private void addAcceptedTables(SchemaTableSet acceptedTables) {
		this.acceptedTables = acceptedTables;
	}

	public void setName(String name) {
		this.name = name;
	}
}
