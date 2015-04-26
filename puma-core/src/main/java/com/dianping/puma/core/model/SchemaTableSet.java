package com.dianping.puma.core.model;

import java.util.*;

public class SchemaTableSet {

	List<SchemaTable> schemaTables = new ArrayList<SchemaTable>();

	public SchemaTableSet() {}

	public List<SchemaTable> listSchemaTables() {
		return this.schemaTables;
	}

	public Map<String, List<String>> mapSchemaTables() {
		Map<String, List<String>> schemaTableMap = new HashMap<String, List<String>>();
		for (SchemaTable schemaTable: listSchemaTables()) {
			List<String> tables = schemaTableMap.get(schemaTable.getSchema());
			if (tables == null) {
				tables = new ArrayList<String>();
			}
			tables.add(schemaTable.getTable());
			schemaTableMap.put(schemaTable.getSchema(), tables);
		}
		return schemaTableMap;
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
