package com.dianping.puma.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SchemaTableSet {

	List<SchemaTable> schemaTables = new ArrayList<SchemaTable>();

	public SchemaTableSet() {}

	public List<SchemaTable> listSchemaTables() {
		return this.schemaTables;
	}

	public void add(SchemaTable schemaTable) {
		for (Iterator<SchemaTable> it = schemaTables.iterator(); it.hasNext();) {
			SchemaTable oriSchemaTable = it.next();
			if (oriSchemaTable.contains(schemaTable)) {
				return;
			} else if (schemaTable.contains(oriSchemaTable)) {
				it.remove();
			}
		}
		schemaTables.add(schemaTable);
	}

	public void addAll(SchemaTableSet schemaTableSet) {
		for (SchemaTable schemaTable: schemaTableSet.listSchemaTables()) {
			add(schemaTable);
		}
	}

	public SchemaTableSet getIncrement(SchemaTableSet schemaTableSet) {
		SchemaTableSet result = new SchemaTableSet();
		for (SchemaTable newSchemaTable: schemaTableSet.listSchemaTables()) {
			boolean contains = false;

			for (SchemaTable oldSchemaTable: this.listSchemaTables()) {
				if (oldSchemaTable.contains(newSchemaTable)) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				result.add(newSchemaTable);
			}
		}
		return result;
	}

	public SchemaTableSet getDecrement(SchemaTableSet schemaTableSet) {
		return schemaTableSet.getIncrement(this);
	}
}
