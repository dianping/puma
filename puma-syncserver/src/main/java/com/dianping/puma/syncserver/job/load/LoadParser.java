package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
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

public class LoadParser {

	private static final VelocityEngine VE;

	private static final Template it; // insert.
	private static final Template dt; // delete.
	private static final Template ut; // update.
	private static final Template rt; // replace.

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
		rt = VE.getTemplate("/sql_template/replaceSql.vm");
	}

	public static String parseSql(RowChangedEvent event) {
		String sql;

		switch (event.getDmlType()) {
		case INSERT:
			sql = parseSql(event, it);
			break;
		case DELETE:
			sql = parseSql(event, dt);
			break;
		case UPDATE:
			sql = parseSql(event, ut);
			break;
		case REPLACE:
			sql = parseSql(event, rt);
			break;
		default:
			sql = null;
		}

		return StringUtils.normalizeSpace(sql);
	}

	public static Object[] parseArgs(RowChangedEvent event) {
		List<Object> args = new ArrayList<Object>();
		Map<String, ColumnInfo> columnInfoMap = event.getColumns();

		switch (event.getDmlType()) {
		case INSERT:
			for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
				args.add(columnName2ColumnInfo.getValue().getNewValue());
			}
			break;
		case DELETE:
			for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
				args.add(columnName2ColumnInfo.getValue().getOldValue());
			}
			break;
		case UPDATE:
			for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
				args.add(columnName2ColumnInfo.getValue().getNewValue());
			}
			for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
				if (columnName2ColumnInfo.getValue().isKey()) {
					args.add(columnName2ColumnInfo.getValue().getOldValue());
				}
			}
			break;
		case REPLACE:
			for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
				args.add(columnName2ColumnInfo.getValue().getNewValue());
			}
			/*
			for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnInfoMap.entrySet()) {
				if (!columnName2ColumnInfo.getValue().isKey()) {
					args.add(columnName2ColumnInfo.getValue().getNewValue());
				}
			}*/
			break;
		}

		return args.toArray();
	}

	private static String parseSql(RowChangedEvent event, Template t) {
		VelocityContext context = new VelocityContext();
		context.put("event", event);
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		return writer.toString();
	}
}
