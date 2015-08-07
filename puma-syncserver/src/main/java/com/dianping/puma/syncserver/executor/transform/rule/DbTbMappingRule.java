package com.dianping.puma.syncserver.executor.transform.rule;

import com.dianping.puma.syncserver.common.binlog.Column;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface DbTbMappingRule extends MappingRule {

	Pair<String, String> map(String database, String table, Map<String, Column> columns);
}
