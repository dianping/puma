package com.dianping.puma.syncserver.executor.transform.rule;

import com.dianping.puma.syncserver.common.binlog.Column;

import javax.sql.DataSource;
import java.util.Map;

public interface DataSourceMappingRule extends MappingRule {

	DataSource map(String database, String table, Map<String, Column> columns);
}
