package com.dianping.puma.syncserver.util.mysql;

import com.dianping.puma.syncserver.common.binlog.Column;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.util.Map;

public class MySqlTemplate {

	private static final VelocityEngine VE;

	public enum RenderTemplate {
		INSERT,
		DELETE,
		UPDATE,
		REPLACE
	}

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

	public static String render(String database, String table, Map<String, Column> columns, RenderTemplate t) {
		VelocityContext context = new VelocityContext();
		context.put("database", database);
		context.put("table", table);
		context.put("columns", columns);

		StringWriter writer = new StringWriter();
		Template template = null;
		switch (t) {
		case INSERT:
			template = it;
			break;
		case DELETE:
			template = dt;
			break;
		case UPDATE:
			template = ut;
			break;
		case REPLACE:
			template = rt;
		}
		template.merge(context, writer);
		return writer.toString();
	}
}
