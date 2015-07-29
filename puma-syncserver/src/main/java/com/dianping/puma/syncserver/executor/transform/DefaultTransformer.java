package com.dianping.puma.syncserver.executor.transform;

import com.dianping.puma.core.dto.mapping.MysqlMapping;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.exception.PumaBinlogException;

import java.util.List;

public class DefaultTransformer implements Transformer {

	private volatile boolean stopped = false;

	private MysqlMapping mysqlMapping;

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		stopped = false;
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;
	}

	@Override
	public ChangedEvent transform(ChangedEvent binlogEvent) {
		if (!stopped) {
			validate(binlogEvent);

			preprocess(binlogEvent);

			String database = mapDatabase(binlogEvent);
			String table = mapTable(binlogEvent);

			binlogEvent.setDatabase(database);
			binlogEvent.setTable(table);
		}

		return null;
	}

	private void validate(ChangedEvent binlogEvent) {
	}

	private ChangedEvent preprocess(ChangedEvent binlogEvent) {
		return null;
	}

	private String mapDatabase(ChangedEvent binlogEvent) {
		String oriDatabase = binlogEvent.getDatabase();

		if (oriDatabase == null) {
			return null;
		}

		String database = mysqlMapping.getDatabase(oriDatabase);
		if (database == null) {
			throw new PumaBinlogException("no mapping database found.");
		}
		return database;
	}

	private String mapTable(ChangedEvent binlogEvent) {
		String oriDatabase = binlogEvent.getDatabase();
		String oriTable = binlogEvent.getTable();

		if (oriDatabase == null || oriTable == null) {
			return null;
		}

		String table = mysqlMapping.getTable(oriDatabase, oriTable);
		if (table == null) {
			throw new PumaBinlogException("no mapping table found.");
		}
		return table;
	}

	private List<String> mapColumns(ChangedEvent binlogEvent) {
		return null;
	}

	private String mapSql(ChangedEvent binlogEvent) {
		return null;
	}

	public void setMysqlMapping(MysqlMapping mysqlMapping) {
		this.mysqlMapping = mysqlMapping;
	}
}
