package com.dianping.puma.syncserver.common.binlog;

import com.dianping.puma.syncserver.executor.transform.rule.DataSourceMappingRule;
import com.dianping.puma.syncserver.executor.transform.rule.DbTbMappingRule;
import org.apache.commons.lang3.tuple.Pair;

import javax.sql.DataSource;

public class DdlEvent extends BinlogEvent {

	@Override
	public DataSource mapDataSource(DataSourceMappingRule rule) {
		return rule.map(database, table, null);
	}

	@Override
	public Pair<String, String> mapDbTb(DbTbMappingRule rule) {
		return rule.map(database, table, null);
	}

	@Override
	public void buildSql() {

	}

	@Override
	public void buildParams() {

	}
}
