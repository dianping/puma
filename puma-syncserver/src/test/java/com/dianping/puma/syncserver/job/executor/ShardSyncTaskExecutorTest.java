package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.ShardSyncTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import junit.framework.Assert;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
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
        this.task.setName("task");
        this.target = new ShardSyncTaskExecutor(task);
        this.target.setConfigCache(configCache);
    }


    @Test
    public void initPumaClientTest() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //prepare
        GroupDataSourceConfig config = new GroupDataSourceConfig();
        DataSourceConfig ds1 = new DataSourceConfig();
        ds1.setCanWrite(true);
        ds1.setJdbcUrl("jdbc:mysql://1.1.1.1:3306/db1?a=b");
        config.getDataSourceConfigs().put("db1", ds1);

        Set<String> tables = Sets.newHashSet("t1", "t2");

        SrcDBInstanceService srcDBInstanceService = mock(SrcDBInstanceService.class);
        SrcDBInstance instance = new SrcDBInstance();
        when(srcDBInstanceService.findByIp("1.1.1.1")).thenReturn(Lists.newArrayList(instance));

        PumaTaskService pumaTaskService = mock(PumaTaskService.class);
        PumaTask pumaTask = new PumaTask();
        PumaServer pumaServer = new PumaServer();
        pumaServer.setHost("2.2.2.2");
        pumaServer.setPort(12345);
        String targetName = "test-puma-test";
        pumaTask.setPumaServerName(pumaServer.getName());
        pumaTask.setName(targetName);
        when(pumaTaskService.findBySrcDBInstanceName(instance.getName())).thenReturn(Lists.newArrayList(pumaTask));

        PumaServerService pumaServerService = mock(PumaServerService.class);
        when(pumaServerService.find(pumaServer.getName())).thenReturn(pumaServer);

        target.setPumaServerService(pumaServerService);
        target.setPumaTaskService(pumaTaskService);
        target.setSrcDBInstanceService(srcDBInstanceService);

        //run
        PumaClient actual = target.initPumaClient("debug", config, tables, "debug", true);


        //verify
        Configuration clientConfig = (Configuration) FieldUtils.readField(actual, "config", true);
        Assert.assertEquals(pumaServer.getHost(), clientConfig.getHost());
        Assert.assertEquals(pumaServer.getPort().intValue(), clientConfig.getPort());
        Assert.assertEquals(pumaTask.getName(), clientConfig.getTarget());
        Assert.assertEquals(true, clientConfig.isNeedDml());
        Assert.assertEquals(false, clientConfig.isNeedDdl());
        Assert.assertEquals(false, clientConfig.isNeedTransactionInfo());
        Assert.assertTrue(clientConfig.getDatabaseTablesMapping().containsKey("db1"));
        Assert.assertEquals(tables.toString(), clientConfig.getDatabaseTablesMapping().get("db1").toString());

        Assert.assertEquals("task-db1-debug", clientConfig.getName());
        System.out.println("PumaClient :" + clientConfig.getName());
    }

    @Test
    public void findTheOnlyWriteDataSourceConfigTestSuccess() {
        GroupDataSourceConfig config = new GroupDataSourceConfig();
        DataSourceConfig ds1 = new DataSourceConfig();
        ds1.setCanWrite(true);
        DataSourceConfig ds2 = new DataSourceConfig();
        ds2.setCanWrite(false);
        config.getDataSourceConfigs().put("1", ds1);
        config.getDataSourceConfigs().put("2", ds2);

        Assert.assertEquals(ds1, target.findTheOnlyWriteDataSourceConfig(config));
    }

    @Test(expected = Exception.class)
    public void findTheOnlyWriteDataSourceConfigTestFailed() {
        GroupDataSourceConfig config = new GroupDataSourceConfig();
        DataSourceConfig ds1 = new DataSourceConfig();
        ds1.setCanWrite(true);
        DataSourceConfig ds2 = new DataSourceConfig();
        ds2.setCanWrite(true);
        config.getDataSourceConfigs().put("1", ds1);
        config.getDataSourceConfigs().put("2", ds2);

        target.findTheOnlyWriteDataSourceConfig(config);
    }

    @Test
    public void initDataSourceTest() {
        ShardSyncTaskExecutor spy = spy(target);

        doAnswer(new Answer<GroupDataSource>() {
            @Override
            public GroupDataSource answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.println("init ds:" + invocationOnMock.getArguments()[0].toString());
                GroupDataSource ds = new GroupDataSource(invocationOnMock.getArguments()[0].toString());
                return ds;
            }
        }).when(spy).initGroupDataSource(anyString());


        TableShardRuleConfig tableShardRuleConfig = buildTableConfigFromFile("initPumaClientsAndDataSourcesTest.json");
        spy.tableShardRuleConfigOrigin = tableShardRuleConfig;
        spy.switchOn = false;
        spy.originDsJdbcRef = "origin";
        spy.initRouterRule();

        spy.initDataSources();

        verify(spy, times(8)).initGroupDataSource(anyString());
        verify(spy, times(1)).initGroupDataSource("origin");
        verify(spy, times(1)).initGroupDataSource("ds0");
        verify(spy, times(1)).initGroupDataSource("ds1");
        verify(spy, times(1)).initGroupDataSource("ds2");
        verify(spy, times(1)).initGroupDataSource("ds3");
        verify(spy, times(1)).initGroupDataSource("ds4");
        verify(spy, times(1)).initGroupDataSource("ds5");
        verify(spy, times(1)).initGroupDataSource("ds8");
    }

    @Test
    public void initPumaClientsTest() {
        ShardSyncTaskExecutor spy = spy(target);

        doReturn(null).when(spy).getGroupDataSourceConfig(anyString());

        doAnswer(new Answer<PumaClient>() {
            @Override
            public PumaClient answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.println("init pumaclient:" + invocationOnMock.getArguments()[1]);
                System.out.println("init pumaclient:" + invocationOnMock.getArguments()[2]);
                System.out.println("init pumaclient:" + invocationOnMock.getArguments()[3]);
                return null;
            }
        }).when(spy).initPumaClient(anyString(), any(GroupDataSourceConfig.class), anySet(), anyString(), anyBoolean());


        TableShardRuleConfig tableShardRuleConfig = buildTableConfigFromFile("initPumaClientsAndDataSourcesTest.json");
        spy.tableShardRuleConfigOrigin = tableShardRuleConfig;
        spy.tableShardRuleConfigList = Lists.newArrayList(tableShardRuleConfig);
        spy.initRouterRule();
        spy.switchOn = false;
        spy.originDsJdbcRef = "origin";
        spy.originDataSource = new GroupDataSource();

        spy.initPumaClient();

        verify(spy, times(7)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), anySet(), anyString(), anyBoolean());
        verify(spy, times(1)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), argThat(new SetMatchers("table1")), eq("migrate"), eq(true));
        verify(spy, times(1)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), argThat(new SetMatchers("table1_0", "table1_1")), eq("master"), eq(false));
        verify(spy, times(1)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), argThat(new SetMatchers("table1_2", "table1_3")), eq("master"), eq(false));
        verify(spy, times(1)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), argThat(new SetMatchers("table1_4", "table1_5")), eq("master"), eq(false));
        verify(spy, times(1)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), argThat(new SetMatchers("table1_6", "table1_7")), eq("master"), eq(false));
        verify(spy, times(1)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), argThat(new SetMatchers("ds1_white")), anyString(), eq(false));
        verify(spy, times(1)).initPumaClient(anyString(), any(GroupDataSourceConfig.class), argThat(new SetMatchers("ds8_white")), anyString(), eq(false));
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
        RouterRuleConfig config = new RouterRuleConfig();
        TableShardRuleConfig tConfig = buildTableConfigFromFile("initPumaClientsAndDataSourcesTest.json");
        config.setTableShardConfigs(Lists.newArrayList(tConfig));

        when(configCache.getProperty("shardds.test.shard")).thenReturn(new Gson().toJson(config));
        when(configCache.getProperty("shardds.test.switch")).thenReturn("false");
        when(configCache.getProperty("shardds.test.origin")).thenReturn("table");

        target.initAndConvertConfig();

        verify(configCache, times(1)).getProperty("shardds.test.shard");
        verify(configCache, times(1)).getProperty("shardds.test.switch");
        verify(configCache, times(1)).getProperty("shardds.test.origin");

        Assert.assertEquals("table1", target.tableShardRuleConfigOrigin.getTableName());
        Assert.assertEquals(1, target.tableShardRuleConfigList.size());
        Assert.assertEquals(1, target.tableShardRuleConfigList.get(0).getDimensionConfigs().size());
        Assert.assertEquals("table", target.originDsJdbcRef);
        Assert.assertEquals(false, target.switchOn);
    }

    @Test
    public void processOnExceptionTest() throws Exception {
        ShardSyncTaskExecutor.Processor processor = spy(target.new Processor("test", null));
        Assert.assertEquals(true, processor.onException(new RowChangedEvent(), new DuplicateKeyException("dp")));
        Assert.assertEquals(true, processor.onException(new RowChangedEvent(), new EmptyResultDataAccessException("error", 1)));
        Assert.assertEquals(false, processor.onException(new RowChangedEvent(), new SQLException("error")));

        processor.tryTimes = processor.MAX_TRY_TIMES + 1;
        Assert.assertEquals(true, processor.onException(new RowChangedEvent(), new SQLException("error")));

        verify(processor, times(4)).logException(any(ChangedEvent.class), any(Exception.class));
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

        target.tableShardRuleConfigOrigin = config;
        target.tableShardRuleConfigList = Lists.newArrayList(config);
        target.initRouterRule();

        Assert.assertEquals(1, target.routerRuleOrigin.getTableShardRules().size());
    }

    private TableShardRuleConfig buildTableConfigFromFile(String file) {
        return new Gson().fromJson(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shard-configs/" + file)), TableShardRuleConfig.class);
    }
}