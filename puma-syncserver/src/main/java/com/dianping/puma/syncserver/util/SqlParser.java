package com.dianping.puma.syncserver.util;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlParser {

	private static final VelocityEngine VE;

	private static final Template it; // insert.
	private static final Template dt; // delete.
	private static final Template ut; // update.

	static {
		VE = new VelocityEngine();
		VE.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
		VE.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		VE.setProperty("class.resource.loader.cache", true);
		VE.setProperty("class.resource.loader.modificationCheckInterval", "-1");
		VE.setProperty("input.encoding", "UTF-8");
		VE.setProperty("runtime.log", "/tmp/velocity.log");
		VE.init();

		it = VE.getTemplate("/sql_template/insertSql.vm");
		dt = VE.getTemplate("/sql_template/deleteSql.vm");
		ut = VE.getTemplate("/sql_template/updateSql.vm");
	}

	public static String parseSql(ChangedEvent binlogEvent) {
		String sql = null;

		if (binlogEvent instanceof DdlEvent) {
			sql = ((DdlEvent) binlogEvent).getSql();
		} else {
			RowChangedEvent rowChangedEvent = (RowChangedEvent) binlogEvent;
			switch (rowChangedEvent.getDmlType()) {
			case INSERT:
				sql = parseSql(rowChangedEvent, it);
				break;
			case DELETE:
				sql = parseSql(rowChangedEvent, dt);
				break;
			case UPDATE:
				sql = parseSql(rowChangedEvent, ut);
				break;
			}
		}

		// Normalize the sql for avoiding confusing mysql bugs.
		return StringUtils.normalizeSpace(sql);
	}

	public static Object[] parseArgs(ChangedEvent binlogEvent) {
		if (binlogEvent instanceof DdlEvent) {
			return null;
		} else {
			RowChangedEvent rowChangedEvent = (RowChangedEvent) binlogEvent;

			List<Object> args = new ArrayList<Object>();
			Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();

			switch (rowChangedEvent.getDmlType()) {
			case INSERT:
				for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
					args.add(columnName2ColumnInfo.getValue().getNewValue());
				}
				break;
			case DELETE:
				for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
					if (columnName2ColumnInfo.getValue().isKey()) {
						args.add(columnName2ColumnInfo.getValue().getOldValue());
					}
				}
				break;
			case UPDATE:
				for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
					args.add(columnName2ColumnInfo.getValue().getNewValue());
				}
				for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
					if (columnName2ColumnInfo.getValue().isKey()) {
						args.add(columnName2ColumnInfo.getValue().getOldValue());
					}
				}
				break;
			}

			return args.toArray();
		}
	}

	private static String parseSql(RowChangedEvent event, Template t) {
		VelocityContext context = new VelocityContext();
		context.put("event", event);
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		return writer.toString();
	}
}
