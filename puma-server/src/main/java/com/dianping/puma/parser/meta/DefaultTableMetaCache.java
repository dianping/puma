package com.dianping.puma.parser.meta;

import com.dianping.puma.core.model.SchemaTable;
import com.dianping.puma.core.model.SchemaTableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultTableMetaCache implements TableMetaCache {

	Map<SchemaTable, TableMeta> tableMetaMap = new HashMap<SchemaTable, TableMeta>();

	public TableMeta getTableMeta(SchemaTable schemaTable) {
		for (Iterator<Map.Entry<SchemaTable, TableMeta>> it = tableMetaMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<SchemaTable, TableMeta> entry = it.next();
			if (entry.getKey().contains(schemaTable)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void clearTableMeta(SchemaTable schemaTable) {
		for (Iterator<Map.Entry<SchemaTable, TableMeta>> it = tableMetaMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<SchemaTable, TableMeta> entry = it.next();
			if (schemaTable.contains(entry.getKey())) {
				it.remove();
			}
		}
	}

	public void refreshTableMeta(SchemaTableSet schemaTableSet) {

	}
}
