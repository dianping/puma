package com.dianping.puma.core.model.event;

import com.dianping.puma.core.model.SchemaTableSet;

public class AcceptedTableChangedEvent extends Event {

	private SchemaTableSet schemaTableSet;

	private SchemaTableSet oriSchemaTableSet;

	public SchemaTableSet getSchemaTableSet() {
		return schemaTableSet;
	}

	public void setSchemaTableSet(SchemaTableSet schemaTableSet) {
		this.schemaTableSet = schemaTableSet;
	}

	public SchemaTableSet getOriSchemaTableSet() {
		return oriSchemaTableSet;
	}

	public void setOriSchemaTableSet(SchemaTableSet oriSchemaTableSet) {
		this.oriSchemaTableSet = oriSchemaTableSet;
	}
}
