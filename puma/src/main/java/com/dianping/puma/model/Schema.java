package com.dianping.puma.model;

public class Schema {

	private String schemaName;

	private static final String ASTERISK = "*";

	public Schema() {}

	public Schema(String schemaName) {
		this.schemaName = schemaName;
	}

	public boolean contains(Schema schema) {
		if (schema != null) {
			return (schemaName.equals(ASTERISK) && schema.getSchemaName() != null) || schemaName.equals(schema.schemaName);
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Schema schema = (Schema) o;

		return schemaName.equals(schema.schemaName);

	}

	@Override
	public int hashCode() {
		return schemaName.hashCode();
	}

	@Override public String toString() {
		return "Schema{" +
				"schemaName='" + schemaName + '\'' +
				'}';
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
}
