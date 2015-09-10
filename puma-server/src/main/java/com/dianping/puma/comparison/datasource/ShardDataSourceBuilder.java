package com.dianping.puma.comparison.datasource;

import com.dianping.zebra.config.LionConfigService;
import com.dianping.zebra.config.LionKey;
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
import com.google.gson.Gson;

import javax.sql.DataSource;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDataSourceBuilder implements DataSourceBuilder {

    private String ruleName;

    private String tableName;

    private int dimensionIndex;

    @Override
    public DataSource build() {
        ShardDataSource ds = new ShardDataSource();
        ds.setRouterFactory(new RouterFactory(ruleName, tableName, dimensionIndex));
        ds.setRuleName(ruleName);
        ds.init();
        return ds;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getDimensionIndex() {
        return dimensionIndex;
    }

    public void setDimensionIndex(int dimensionIndex) {
        this.dimensionIndex = dimensionIndex;
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
                        @Override
                        public boolean apply(TableShardRuleConfig input) {
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

        @Override
        public ShardRouter getRouter() {
            ShardRouterImpl router = new ShardRouterImpl();
            RouterRule routerRule = build(routerConfig);
            router.setRouterRule(routerRule);

            return router;
        }
    }
}
