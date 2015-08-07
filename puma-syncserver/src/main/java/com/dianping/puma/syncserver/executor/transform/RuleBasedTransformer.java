package com.dianping.puma.syncserver.executor.transform;

import com.dianping.puma.syncserver.common.AbstractLifeCycle;
import com.dianping.puma.syncserver.common.binlog.BinlogEvent;
import com.dianping.puma.syncserver.common.binlog.EventType;
import com.dianping.puma.syncserver.executor.transform.rule.DataSourceMappingRule;
import com.dianping.puma.syncserver.executor.transform.rule.DbTbMappingRule;
import org.apache.commons.lang3.tuple.Pair;

import javax.sql.DataSource;

public class RuleBasedTransformer extends AbstractLifeCycle implements Transformer {

	protected DataSourceMappingRule dataSourceMappingRule;

	protected DbTbMappingRule dbTbMappingRule;

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public void transform(BinlogEvent binlogEvent) {
		DataSource dataSource = binlogEvent.mapDataSource(dataSourceMappingRule);
		Pair<String, String> dbtb = binlogEvent.mapDbTb(dbTbMappingRule);

		binlogEvent.setDataSource(dataSource);
		binlogEvent.setDatabase(dbtb.getLeft());
		binlogEvent.setTable(dbtb.getRight());

		binlogEvent.buildSql();
		binlogEvent.buildParams();
	}

	public void setDataSourceMappingRule(DataSourceMappingRule dataSourceMappingRule) {
		this.dataSourceMappingRule = dataSourceMappingRule;
	}

	public void setDbTbMappingRule(DbTbMappingRule dbTbMappingRule) {
		this.dbTbMappingRule = dbTbMappingRule;
	}
}
