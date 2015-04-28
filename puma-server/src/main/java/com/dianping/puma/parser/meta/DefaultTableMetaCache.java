package com.dianping.puma.parser.meta;

import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultTableMetaCache implements TableMetaCache {

	Map<Table, TableMeta> tableMetaMap = new HashMap<Table, TableMeta>();

	public TableMeta getTableMeta(Table table) {
		for (Iterator<Map.Entry<Table, TableMeta>> it = tableMetaMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Table, TableMeta> entry = it.next();
			if (entry.getKey().contains(table)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void clearTableMeta(Table table) {
		for (Iterator<Map.Entry<Table, TableMeta>> it = tableMetaMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Table, TableMeta> entry = it.next();
			if (table.contains(entry.getKey())) {
				it.remove();
			}
		}
	}

	public void refreshTableMeta(TableSet tableSet) {

	}
}
