package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ShardSyncTaskExecutorTest {
    ShardSyncTask task = new ShardSyncTask();
    ShardSyncTaskExecutor target;
    ConfigCache configCache = mock(ConfigCache.class);

    @Before
    public void init() {
        this.task.setRuleName("test");
        this.task.setTableName("table1");
        this.target = new ShardSyncTaskExecutor(task);
        this.target.setConfigCache(configCache);
    }

    @Test
    public void initAndConvertConfigTest() throws LionException {
        RouterRuleConfig config = buildRouterRuleConfig();
        when(configCache.getProperty("shardds.test.shard")).thenReturn(new Gson().toJson(config));
        when(configCache.getProperty("shardds.test.switch")).thenReturn("false");
        when(configCache.getProperty("shardds.test.origin")).thenReturn("table");

        target.initAndConvertConfig();

        verify(configCache, times(1)).getProperty("shardds.test.shard");
        verify(configCache, times(1)).getProperty("shardds.test.switch");
        verify(configCache, times(1)).getProperty("shardds.test.origin");

        Assert.assertEquals("table1", target.tableShardRuleConfig.getTableName());
        Assert.assertEquals("table", target.originGroupDataSource);
        Assert.assertEquals(false, target.switchOn);
    }

    @Test
    public void initRouterConfigTest() {
        TableShardRuleConfig config = new TableShardRuleConfig();
        config.setTableName("test1");

        TableShardDimensionConfig dimensionConfig = new TableShardDimensionConfig();
        dimensionConfig.setTableName("test1");
        dimensionConfig.setDbIndexes("db1");
        dimensionConfig.setDbRule("(#id# % 4 / 4)");
        dimensionConfig.setTbRule("(#id# % 4)");
        dimensionConfig.setTbSuffix("alldb:[_0,_3]");

        config.setDimensionConfigs(Lists.newArrayList(dimensionConfig));

        target.tableShardRuleConfig = config;
        target.initRouterConfig();

        Assert.assertEquals(1, target.routerRule.getTableShardRules().size());
    }

    private RouterRuleConfig buildRouterRuleConfig() {
        RouterRuleConfig config = new RouterRuleConfig();
        TableShardRuleConfig tableConfig1 = new TableShardRuleConfig();
        tableConfig1.setTableName("table1");
        TableShardRuleConfig tableConfig2 = new TableShardRuleConfig();
        tableConfig2.setTableName("table2");
        config.setTableShardConfigs(Lists.newArrayList(tableConfig1, tableConfig2));
        return config;
    }
}