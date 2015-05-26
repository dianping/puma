package com.dianping.puma.core.model;

public class Table {

	// schema.table: a specific table.
	// schema.*    : all the tables in the given schema.
	// *.table     : all the schemas with the given table.
	private String schemaName;

	private String tableName;

	private static final String ASTERISK = "*";

	public Table() {
	}

	public Table(String schemaName, String tableName) {
		setSchemaName(schemaName);
		setTableName(tableName);
	}

	public boolean contains(Table table) {
		if (table != null) {
			return compare(schemaName, table.schemaName) && compare(tableName, table.tableName);
		}
		return false;
	}

	private boolean compare(String a, String b) {
		return !(a == null || b == null) && (a.equals(ASTERISK) || a.equals(b));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o){
			return true;
		}
		if (o == null || !(o instanceof Table)){
			return false;
		}

		Table table = (Table) o;

		if (!schemaName.equals(table.schemaName))
			return false;
		if (!tableName.equals(table.tableName))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = schemaName.hashCode();
		result = 37 * result + tableName.hashCode();
		return result;
	}

	@Override public String toString() {
		return "Table{" +
				"schemaName='" + schemaName + '\'' +
				", tableName='" + tableName + '\'' +
				'}';
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
