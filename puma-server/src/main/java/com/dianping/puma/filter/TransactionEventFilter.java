package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.Schema;
import com.dianping.puma.core.model.SchemaSet;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.core.model.event.AcceptedTableChangedEvent;
import com.dianping.puma.core.model.event.EventListener;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionEventFilter extends AbstractEventFilter implements EventListener<AcceptedTableChangedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionEventFilter.class);

	private String name;

	private boolean begin = true;

	private boolean commit = true;

	private SchemaSet acceptedSchemas = new SchemaSet();

	protected boolean checkEvent(ChangedEvent changedEvent) {
		if (changedEvent instanceof RowChangedEvent) {

			// Transaction or not.
			if (!((RowChangedEvent) changedEvent).isTransactionBegin() && !((RowChangedEvent) changedEvent)
					.isTransactionCommit()) {
				return true;
			}

			// Need begin or not.
			if (((RowChangedEvent) changedEvent).isTransactionBegin() && !begin) {
				return false;
			}

			// Need commit or not.
			if (((RowChangedEvent) changedEvent).isTransactionCommit() && !commit) {
				return false;
			}

			// In accepted table list.
			Schema schema = new Schema(changedEvent.getDatabase());
			if (!acceptedSchemas.contains(schema)) {
				return false;
			}

			return true;
		}

		return true;
	}

	@Subscribe
	public void onEvent(AcceptedTableChangedEvent event) {
		if (event.getName().equals(name)) {
			LOG.info("`TransactionEventFilter` receives event: {}.", event.toString());

			TableSet tableSet = event.getTableSet();
			if (tableSet != null) {
				SchemaSet schemaSet = new SchemaSet();
				for (Table table : tableSet.listSchemaTables()) {
					schemaSet.add(new Schema(table.getSchemaName()));
				}
				setAcceptedSchemas(schemaSet);
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBegin(boolean begin) {
		this.begin = begin;
	}

	public void setCommit(boolean commit) {
		this.commit = commit;
	}

	public void setAcceptedSchemas(SchemaSet acceptedSchemas) {
		this.acceptedSchemas = acceptedSchemas;
	}
}
