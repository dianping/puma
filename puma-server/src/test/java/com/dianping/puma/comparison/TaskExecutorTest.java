package com.dianping.puma.comparison;

import com.dianping.zebra.config.LionConfigService;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.shard.jdbc.ShardDataSource;
import com.dianping.zebra.shard.router.AbstractDataSourceRouterFactory;
import com.dianping.zebra.shard.router.DataSourceRouterFactory;
import com.dianping.zebra.shard.router.ShardRouter;
import com.dianping.zebra.shard.router.ShardRouterImpl;
import com.dianping.zebra.shard.router.rule.RouterRule;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.Date;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutorTest {

	@Test
	public void fetchSourceDebug() throws Exception {
		final TaskExecutor target = new TaskExecutor();

		GroupDataSource sourceDs = new GroupDataSource();
		sourceDs.setJdbcRef("unifiedorder0");
		sourceDs.init();

		ShardDataSource targetDs = new ShardDataSource();
		targetDs.setRouterFactory(new RouterFactory("unifiedorder", "UOD_Order", 3));
		targetDs.setRuleName("unifiedorder");
		targetDs.init();

		target.setSourceTable("UOD_Order0");
		target.setSourceDs(sourceDs);

		target.setTargetTable("UOD_Order");
		target.setTargetDs(targetDs);

		target.setColumns(Sets.newHashSet("*"));
		target.setLastTime(new Date(1430956800000l));
		target.setKeys(Sets.newHashSet("AddTime", "OrderID"));

		target.start();
	}

	public static class RouterFactory extends AbstractDataSourceRouterFactory
		implements DataSourceRouterFactory {

		private final RouterRuleConfig routerConfig;

		public RouterFactory(String ruleName, final String tableName, final int index) {
			LionConfigService configService = LionConfigService.getInstance();

			RouterRuleConfig newRouterConfig = new Gson()
				.fromJson(configService.getProperty(LionKey.getShardConfigKey(ruleName)),
					RouterRuleConfig.class);

			TableShardRuleConfig tableRuleConfig = FluentIterable.from(newRouterConfig.getTableShardConfigs())
				.firstMatch(new Predicate<TableShardRuleConfig>() {
					@Override public boolean apply(TableShardRuleConfig input) {
						return input.getTableName().equalsIgnoreCase(tableName);
					}
				}).get();

			TableShardDimensionConfig dimensionCOnfig = tableRuleConfig.getDimensionConfigs().get(index);
			dimensionCOnfig.setTableName(tableName);
			dimensionCOnfig.setMaster(true);

			this.routerConfig = new RouterRuleConfig();
			TableShardRuleConfig newTableShardRuleConfig = new TableShardRuleConfig();
			newTableShardRuleConfig.setTableName(tableName);
			newTableShardRuleConfig.setDimensionConfigs(Lists.newArrayList(dimensionCOnfig));
			this.routerConfig.setTableShardConfigs(Lists.newArrayList(newTableShardRuleConfig));
		}

		@Override public ShardRouter getRouter() {
			ShardRouterImpl router = new ShardRouterImpl();
			RouterRule routerRule = build(routerConfig);
			router.setRouterRule(routerRule);

			return router;
		}
	}
}