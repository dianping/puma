package com.dianping.puma.parser.meta;

import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;

public interface TableMetaCache {

	TableMeta getTableMeta(Table table);

	void clearTableMeta(Table table);

	void refreshTableMeta(TableSet tableSet);
}
