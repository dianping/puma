package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.InputStreamReader;
import java.util.Set;

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
    public void initPumaClientsAndDataSourcesTest() {
        ShardSyncTaskExecutor spy = spy(target);

        doAnswer(new Answer<GroupDataSource>() {
            @Override
            public GroupDataSource answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.println("init ds:" + invocationOnMock.getArguments()[0].toString());
                GroupDataSource ds = new GroupDataSource(invocationOnMock.getArguments()[0].toString());
                return ds;
            }
        }).when(spy).initGroupDataSource(anyString());

        doAnswer(new Answer<PumaClient>() {
            @Override
            public PumaClient answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.println("init pumaclient:" + invocationOnMock.getArguments()[1]);
                System.out.println("init pumaclient:" + invocationOnMock.getArguments()[2]);
                return null;
            }
        }).when(spy).initPumaClient(any(GroupDataSourceConfig.class), anyLong(), anySet());


        TableShardRuleConfig tableShardRuleConfig = buildTableConfigFromFile("initPumaClientsAndDataSourcesTest.json");
        spy.tableShardRuleConfig = tableShardRuleConfig;
        spy.initRouterConfig();
        spy.switchOn = false;
        spy.originGroupDataSource = "origin";

        spy.initPumaClientsAndDataSources();


        verify(spy, times(7)).initGroupDataSource(anyString());
        verify(spy, times(1)).initGroupDataSource("origin");
        verify(spy, times(1)).initGroupDataSource("ds0");
        verify(spy, times(2)).initGroupDataSource("ds1");
        verify(spy, times(1)).initGroupDataSource("ds2");
        verify(spy, times(1)).initGroupDataSource("ds3");
        verify(spy, times(1)).initGroupDataSource("ds8");
        verify(spy, times(0)).initGroupDataSource("ds4");
        verify(spy, times(0)).initGroupDataSource("ds5");

        verify(spy, times(8)).initPumaClient(any(GroupDataSourceConfig.class), anyLong(), anySet());
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_LATEST), argThat(new SetMatchers("table1")));
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_OLDEST), argThat(new SetMatchers("table1")));
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_LATEST), argThat(new SetMatchers("table1_0", "table1_1")));
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_LATEST), argThat(new SetMatchers("table1_2", "table1_3")));
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_LATEST), argThat(new SetMatchers("table1_4", "table1_5")));
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_LATEST), argThat(new SetMatchers("table1_6", "table1_7")));
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_LATEST), argThat(new SetMatchers("ds1_white")));
        verify(spy, times(1)).initPumaClient(any(GroupDataSourceConfig.class), eq(SubscribeConstant.SEQ_FROM_LATEST), argThat(new SetMatchers("ds8_white")));
    }

    class SetMatchers extends ArgumentMatcher<Set<String>> {
        private final String[] expects;

        public SetMatchers(String... expects) {
            this.expects = expects;
        }

        @Override
        public boolean matches(Object argument) {
            Set<String> args = (Set<String>) argument;

            if (expects.length != args.size()) {
                return false;
            }

            for (String expect : expects) {
                if (!args.contains(expect)) {
                    return false;
                }
            }

            return true;
        }
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
        dimensionConfig.setMaster(true);
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

    private TableShardRuleConfig buildTableConfigFromFile(String file) {
        return new Gson().fromJson(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shard-configs/" + file)), TableShardRuleConfig.class);
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