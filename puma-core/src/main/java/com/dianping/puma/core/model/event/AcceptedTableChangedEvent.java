package com.dianping.puma.core.model.event;

import com.dianping.puma.core.model.SchemaTableSet;

public class AcceptedTableChangedEvent extends InnerEvent {

	private SchemaTableSet schemaTableSet;

	public SchemaTableSet getSchemaTableSet() {
		return schemaTableSet;
	}

	public void setSchemaTableSet(SchemaTableSet schemaTableSet) {
		this.schemaTableSet = schemaTableSet;
	}
}
