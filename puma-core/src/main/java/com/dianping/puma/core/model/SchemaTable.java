package com.dianping.puma.core.model;

import org.apache.commons.lang.StringUtils;

public class SchemaTable {

	// "name" for a specific schema, "*" for all the schema.
	private String schema;

	// "name" for a specific table, "*" for all the schema.
	private String table;

	// Full name format "schema.table".
	private static final String SPLIT = ".";

	// Match all the name.
	private static final String STAR = "*";

	public SchemaTable() {}

	public SchemaTable(String schema, String table) {
		setSchema(schema);
		setTable(table);
	}

	public SchemaTable(String fullName) {
		String names[] = StringUtils.split(fullName, SPLIT);
		if (names != null) {
			if (names.length == 1) {
				// If not split found, set the name as the table name.
				setSchema(null);
				setTable(names[0]);
			} else {
				setSchema(names[0]);
				setTable(names[1]);
			}
		}
	}

	public boolean contains(SchemaTable schemaTable) {
		return compare(this.schema, schemaTable.schema) && compare(this.table, schemaTable.table);
	}

	public String getFullName() {
		return schema + SPLIT + table;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	private boolean compare(String a, String b) {
		return a.equals(STAR) || a.equals(b);
	}
}
