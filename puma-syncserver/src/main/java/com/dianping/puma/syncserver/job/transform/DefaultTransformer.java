package com.dianping.puma.syncserver.job.transform;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.biz.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.util.sql.DDLParser;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.transform.exception.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultTransformer implements Transformer {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultTransformer.class);

	private boolean stopped = true;

	private String title = "Transform-";

	private String name;

	private MysqlMapping mysqlMapping;

	public DefaultTransformer() {
	}

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

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
	public void transform(ChangedEvent event) throws TransformException {
		// Prepare.
		if (event instanceof RowChangedEvent) {
			RowChangedEvent row = (RowChangedEvent) event;
			if (row.getDmlType() == DMLType.INSERT) {
				row.setDmlType(DMLType.REPLACE);
			}
		}

		transformColumn(event);
		transformTable(event);
		transformSchema(event);
		transformSQL(event);
	}

	private void transformSchema(ChangedEvent event) {
		String oriSchema = event.getDatabase();
		if (oriSchema != null) {
			String schema = mysqlMapping.getDatabase(oriSchema);
			if (schema == null) {
				LOG.error("Transformer({}) transform schema failure for event({}).", title + name, event.toString());
				throw new TransformException(-1,
						String.format("Transformer(%s) transform schema failure for event(%s).", title + name,
								event.toString()));
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
				LOG.error("Transformer({}) transform table failure for event({}).", title + name, event.toString());
				throw new TransformException(-1,
						String.format("Transformer(%s) transform table failure for event(%s).", title + name,
								event.toString()));
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
					LOG.error("Transformer({}) transform column failure for event({}).", title + name, event.toString());
					throw new TransformException(-1,
							String.format("Transformer(%s) transform column failure for event(%s).", title + name,
									dmlEvent.toString()));
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
				LOG.error("Transformer({}) transform sql failure for event({}).", title + name, event.toString());
				throw new TransformException(-1,
						String.format("Transformer(%s) transform sql failure for event(%s).", title + name,
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
