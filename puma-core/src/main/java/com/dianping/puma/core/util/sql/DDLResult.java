package com.dianping.puma.core.util.sql;

public class DDLResult {

	private DDLType type;

	private String schemaName;

	private String tableName;

	// Original schema name in `RENAME`.
	private String oriSchemaName;

	// Original table name in `RENAME`.
	private String oriTableName;

	public DDLResult() {}

	public DDLResult(DDLType type) {
		this.type = type;
	}

	public DDLResult(DDLType type, String schemaName, String tableName) {
		this.type = type;
		this.schemaName = schemaName;
		this.tableName = tableName;
	}

	public DDLResult(DDLType type, String schemaName, String tableName, String oriSchemaName, String oriTableName) {
		this.type = type;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.oriSchemaName = oriSchemaName;
		this.oriTableName = oriTableName;
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

	public String getOriSchemaName() {
		return oriSchemaName;
	}

	public void setOriSchemaName(String oriSchemaName) {
		this.oriSchemaName = oriSchemaName;
	}

	public String getOriTableName() {
		return oriTableName;
	}

	public void setOriTableName(String oriTableName) {
		this.oriTableName = oriTableName;
	}

	public DDLType getType() {
		return type;
	}

	public void setType(DDLType type) {
		this.type = type;
	}


}
