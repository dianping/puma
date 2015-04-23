package com.dianping.puma.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.model.SchemaTable;
import com.dianping.puma.core.model.SchemaTableSet;
import com.dianping.puma.core.model.event.AcceptedTableChangedEvent;
import com.dianping.puma.core.model.event.EventListener;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.lang.StringUtils;

import com.dianping.puma.core.event.ChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.puma.core.event.Event;

public class DbTbEventFilter extends AbstractEventFilter implements EventListener<AcceptedTableChangedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(DbTbEventFilter.class);

	private String name;

	private SchemaTableSet schemaTableSet = new SchemaTableSet();

	private Map<String, Boolean> dbtbMap = new ConcurrentHashMap<String, Boolean>();

	private List<String> tbPrefixList = new ArrayList<String>();

	private static final String DB_TB_SPLIT_STR = ".";

	private static final String SUFIX_ANY = "*";

	public void init(String[] dts) {
		if (dts != null && dts.length > 0) {
			for (String dt : dts) {
				String[] parts = dt.split("\\.");
				String dbName = parts[0].trim().toLowerCase();
				String tbName = parts[1].trim().toLowerCase();

				if (!tbName.endsWith(SUFIX_ANY)) {
					dbtbMap.put(dbName + DB_TB_SPLIT_STR + tbName, true);
				} else {
					if (SUFIX_ANY.length() < tbName.length()) {
						tbPrefixList.add(dbName + DB_TB_SPLIT_STR
								+ tbName.substring(0, tbName.length() - SUFIX_ANY.length()));
					} else if (SUFIX_ANY.length() == tbName.length()) {
						tbPrefixList.add(dbName + DB_TB_SPLIT_STR);
					}

				}

				dbtbMap.put(dbName + DB_TB_SPLIT_STR, true);

			}
		}
	}

	protected boolean checkEvent(ChangedEvent event) {
		if (event != null) {
			return newCheckEvent(event) || oldCheckEvent(event);
		}
		return false;
	}

	private boolean newCheckEvent(ChangedEvent changedEvent) {
		SchemaTable eventSchemaTable = new SchemaTable();
		eventSchemaTable.setSchema(changedEvent.getDatabase());
		eventSchemaTable.setTable(changedEvent.getTable());

		for (SchemaTable schemaTable: schemaTableSet.listSchemaTables()) {
			if (schemaTable.contains(eventSchemaTable)) {
				return true;
			}
		}

		return false;
	}

	private boolean oldCheckEvent(ChangedEvent event) {
		if (event != null) {
			String dbName = StringUtils.trimToEmpty(event.getDatabase()).toLowerCase();
			String tbName = StringUtils.trimToEmpty(event.getTable()).toLowerCase();
			String key = dbName + DB_TB_SPLIT_STR + tbName;
			if (dbtbMap.get(key) != null) {
				return true;
			} else {
				for (String prefix : tbPrefixList) {
					if (key.startsWith(prefix)) {
						return true;
					}
				}
				return false;
			}
		} else {
			return false;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addAcceptedTables(SchemaTableSet schemaTableSet) {
		this.schemaTableSet = schemaTableSet;
	}

	@Subscribe
	public void onEvent(AcceptedTableChangedEvent event) {
		if (event.getName().equals(name)) {
			LOG.info("`DbTbEventFilter` receives event: {}.", event.toString());

			SchemaTableSet schemaTableSet = event.getSchemaTableSet();
			addAcceptedTables(schemaTableSet);
		}
	}
}