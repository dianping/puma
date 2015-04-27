package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.model.SchemaTable;
import com.dianping.puma.core.model.SchemaTableSet;
import com.dianping.puma.core.model.event.AcceptedTableChangedEvent;
import com.dianping.puma.core.model.event.EventListener;
import com.dianping.puma.core.util.sql.DDLType;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DDLEventFilter extends AbstractEventFilter implements EventListener<AcceptedTableChangedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(DDLEventFilter.class);

	private String name;

	private boolean ddl = true;

	private SchemaTableSet acceptedTables = new SchemaTableSet();

	private List<DDLType> ddlTypes = new ArrayList<DDLType>();

	public void init(boolean ddl, List<DDLType> ddlTypes) {
		this.ddl = ddl;
		this.ddlTypes = ddlTypes;
	}

	protected boolean checkEvent(ChangedEvent changedEvent) {
		if (changedEvent instanceof DdlEvent) {

			// Need ddl or not.
			if (!ddl) {
				return false;
			}

			// In supported ddl type list or not.
			if (!ddlTypes.contains(((DdlEvent) changedEvent).getDDLType())) {
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
			LOG.info("`DDLEventFilter` receives event: {}.", event.toString());

			SchemaTableSet schemaTableSet = event.getSchemaTableSet();
			setAcceptedTables(schemaTableSet);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDdl(boolean ddl) {
		this.ddl = ddl;
	}

	public void setAcceptedTables(SchemaTableSet acceptedTables) {
		this.acceptedTables = acceptedTables;
	}

	public void setDdlTypes(List<DDLType> ddlTypes) {
		this.ddlTypes = ddlTypes;
	}
}
