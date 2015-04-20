package com.dianping.puma.parser.meta;

public interface TableMetaCache {

	TableMeta getTableMeta(String schema, String table);

	void clearTableMeta(String schema, String table);

	void clearTableMetaBySchemaName(String schema);

	void clearTableMetaAll();

	void refreshTableMeta(Tables tables);
}
