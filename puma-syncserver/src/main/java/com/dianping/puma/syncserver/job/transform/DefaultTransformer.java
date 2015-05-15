package com.dianping.puma.syncserver.job.transform;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.util.sql.DDLParser;
import com.dianping.puma.syncserver.job.transform.exception.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultTransformer implements Transformer {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultTransformer.class);

	private String name;

	private boolean stopped = true;

	private TransformException transformException = null;

	private MysqlMapping mysqlMapping;

	public DefaultTransformer() {
	}

	@Override
	public void start() {
		stopped = false;
		transformException = null;
	}

	@Override
	public void stop() {
		stopped = true;
	}

	@Override
	public TransformException exception() {
		return transformException;
	}

	@Override
	public void transform(ChangedEvent event) throws TransformException {
		if (stopped) {
			LOG.error("Transformer({}) is stopped for event({}).", name, event.toString());
			throw new TransformException(0,
					String.format("Transformer(%s) is stopped for event(%s).", name, event.toString()));
		}
		transformSQL(event);
		transformColumn(event);
		transformTable(event);
		transformSchema(event);
	}

	private void transformSchema(ChangedEvent event) {
		String oriSchema = event.getDatabase();
		if (oriSchema != null) {
			String schema = mysqlMapping.getSchema(oriSchema);
			if (schema == null) {
				LOG.error("Transformer({}) transform schema failure for event({}).", name, event.toString());
				throw new TransformException(1,
						String.format("Transformer(%s) transform schema failure for event(%s).", name, event.toString()));
			} else {
				event.setDatabase(schema);
			}
		}
	}

	private void transformTable(ChangedEvent event) {
		String oriSchema = event.getDatabase();
		String oriTable = event.getTable();
		if (oriTable != null) {
			String table = mysqlMapping.getTable(oriSchema, oriTable);
			if (table == null) {
				LOG.error("Transformer({}) transform table failure for event({}).", name, event.toString());
				throw new TransformException(2, String.format("Transformer(%s) transform table failure for event(%s).",
						name, event.toString()));
			} else {
				event.setTable(table);
			}
		}
	}

	private void transformColumn(ChangedEvent event) {
		if (event instanceof RowChangedEvent) {
			RowChangedEvent dmlEvent = (RowChangedEvent) event;

			String oriSchema = event.getDatabase();
			String oriTable = event.getTable();

			Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
			for (String oriColumn : dmlEvent.getColumns().keySet()) {
				String column = mysqlMapping.getColumn(oriSchema, oriTable, oriColumn);
				if (column == null) {
					LOG.error("Transformer({}) transform column failure for event({}).", name, event.toString());
					throw new TransformException(3, String.format("Transformer(%s) transform column failure for event(%s).",
							name, dmlEvent.toString()));
				} else {
					columns.put(column, dmlEvent.getColumns().get(oriColumn));
				}
			}
			dmlEvent.setColumns(columns);
		}
	}

	private void transformSQL(ChangedEvent event) {
		if (event instanceof DdlEvent) {
			DdlEvent ddlEvent = (DdlEvent) event;
			String sql = DDLParser
					.replaceDdl(ddlEvent.getSql(), ddlEvent.getDatabase(), ddlEvent.getTable(), ddlEvent.getDDLType());
			if (sql == null) {
				LOG.error("Transformer({}) transform sql failure for event({}).", name, event.toString());
				throw new TransformException(4, String.format("Transformer(%s) transform sql failure for event(%s).", name,
						ddlEvent.toString()));
			}

			ddlEvent.setSql(sql);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMysqlMapping(MysqlMapping mysqlMapping) {
		this.mysqlMapping = mysqlMapping;
	}
}
