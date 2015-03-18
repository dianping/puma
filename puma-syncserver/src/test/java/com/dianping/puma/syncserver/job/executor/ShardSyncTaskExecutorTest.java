package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ShardSyncTaskExecutorTest {

    @Test
    public void initAndConvertConfigTest() throws LionException {
        ShardSyncTask task = new ShardSyncTask();
        task.setRuleName("test");
        task.setTableName("table1");
        ShardSyncTaskExecutor target = new ShardSyncTaskExecutor(task);

        RouterRuleConfig config = new RouterRuleConfig();
        TableShardRuleConfig tableConfig1 = new TableShardRuleConfig();
        tableConfig1.setTableName("table1");
        TableShardRuleConfig tableConfig2 = new TableShardRuleConfig();
        tableConfig2.setTableName("table2");
        config.setTableShardConfigs(Lists.newArrayList(tableConfig1, tableConfig2));

        ConfigCache configCache = mock(ConfigCache.class);
        when(configCache.getProperty("shardds.test.shard")).thenReturn(new Gson().toJson(config));
        when(configCache.getProperty("shardds.test.switch")).thenReturn("false");
        when(configCache.getProperty("shardds.test.origin")).thenReturn("table");

        target.setConfigCache(configCache);

        target.initAndConvertConfig();

        verify(configCache, times(1)).getProperty("shardds.test.shard");
        verify(configCache, times(1)).getProperty("shardds.test.switch");
        verify(configCache, times(1)).getProperty("shardds.test.origin");

        Assert.assertEquals("table1", target.tableShardRuleConfig.getTableName());
        Assert.assertEquals("table", target.originGroupDataSource);
        Assert.assertEquals(false, target.switchOn);
    }
}