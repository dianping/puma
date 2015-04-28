package com.dianping.puma.core.model;

import org.apache.commons.lang.StringUtils;

public class SchemaTable {

	// schema.table: a specific table.
	// schema.*    : all the tables in the given schema.
	// *.table     : all the schemas with the given table.
	// schema.null : schema, with no table.
	private String schema;
	private String table;

	// Full name format "schema.table".
	private static final String SPLIT = ".";

	// Meaning all.
	private static final String STAR = "*";

	// Meaning any.
	private static final String HYPHEN = "-";

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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SchemaTable that = (SchemaTable) o;

		if (schema != null ? !schema.equals(that.schema) : that.schema != null)
			return false;
		if (table != null ? !table.equals(that.table) : that.table != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = schema != null ? schema.hashCode() : 0;
		result = 31 * result + (table != null ? table.hashCode() : 0);
		return result;
	}

	public boolean isSchemaAsterisk() {
		return schema.equals(STAR);
	}

	public boolean isTableAsterisk() {
		return table.equals(STAR);
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
		if (a == null || b == null) {
			return false;
		} else {
			return a.equals(STAR) || a.equals(b);
		}
	}
}
