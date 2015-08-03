package com.dianping.puma.filter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.core.model.event.AcceptedTableChangedEvent;
import com.dianping.puma.core.model.event.EventListener;
import com.dianping.puma.core.util.sql.DDLType;
import com.google.common.eventbus.Subscribe;

public class TableMetaRefreshFilter implements EventListener<AcceptedTableChangedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(TableMetaRefreshFilter.class);

	private String name;

	private TableSet acceptedTables = new TableSet();

	private static final List<DDLType> ddlTypes = new ArrayList<DDLType>();

	static {
		ddlTypes.add(DDLType.ALTER_TABLE);
	}

	public boolean accept(ChangedEvent changedEvent) {

		if (changedEvent == null) {
			return false;
		}

		if (!ddlTypes.contains(((DdlEvent) changedEvent).getDDLType())) {
			return false;
		}

		Table table = new Table(changedEvent.getDatabase(), changedEvent.getTable());
		if (!acceptedTables.contains(table)) {
			return false;
		}
		return true;
	}

	@Subscribe
	public void onEvent(AcceptedTableChangedEvent event) {
		if (event.getName().equals(name)) {
			LOG.info("`TableMetRefreshFilter` receives event: {}.", event.toString());

			TableSet tableSet = event.getTableSet();
			if (tableSet != null) {
				setAcceptedTables(tableSet);
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TableSet getAcceptedTables() {
		return acceptedTables;
	}

	public void setAcceptedTables(TableSet acceptedTables) {
		this.acceptedTables = acceptedTables;
	}

}
