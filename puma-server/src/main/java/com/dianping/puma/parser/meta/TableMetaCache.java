package com.dianping.puma.parser.meta;

import com.dianping.puma.core.model.SchemaTable;
import com.dianping.puma.core.model.SchemaTableSet;

public interface TableMetaCache {

	TableMeta getTableMeta(SchemaTable schemaTable);

	void clearTableMeta(SchemaTable schemaTable);

	void refreshTableMeta(SchemaTableSet schemaTableSet);
}
