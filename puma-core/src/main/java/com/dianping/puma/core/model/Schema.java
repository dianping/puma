package com.dianping.puma.core.model;

public class Schema {

	private String schemaName;

	private static final String ASTERISK = "*";

	public Schema() {}

	public Schema(String schemaName) {
		this.schemaName = schemaName;
	}

	public boolean contains(Schema schema) {
		if (schema != null) {
			return this.schemaName.equals(ASTERISK) || this.schemaName.equals(schema.schemaName);
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

		if (!schemaName.equals(schema.schemaName))
			return false;

		return true;
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
