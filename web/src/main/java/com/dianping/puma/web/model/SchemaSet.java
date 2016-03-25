package com.dianping.puma.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SchemaSet {

	List<Schema> schemas = new ArrayList<Schema>();

	public SchemaSet() {}

	public List<Schema> listSchemas() {
		if (schemas != null) {
			return schemas;
		}
		return Collections.emptyList();
	}

	public void add(Schema schema) {
		for (Iterator<Schema> it = schemas.iterator(); it.hasNext();) {
			Schema oriSchema = it.next();
			if (oriSchema.contains(schema)) {
				return;
			} else if (schema.contains(oriSchema)) {
				it.remove();
			}
		}
		schemas.add(schema);
	}

	public void addAll(SchemaSet schemaSet) {
		if (schemaSet != null) {
			for (Schema schema: schemaSet.listSchemas()) {
				add(schema);
			}
		}
	}

	public boolean contains(Schema schema) {
		for (Schema oriSchema: schemas) {
			if (oriSchema.contains(schema)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "SchemaSet{" +
				"schemas=" + schemas +
				'}';
	}
}
