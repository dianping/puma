package com.dianping.puma.parser;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.filter.*;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.model.Table;
import com.dianping.puma.model.TableSet;
import com.dianping.puma.parser.meta.DefaultTableMetaInfoFetcher;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatcherImpl;
import com.dianping.puma.storage.channel.ChannelFactory;
import com.dianping.puma.storage.channel.ReadChannel;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.dianping.puma.storage.manage.LocalFileInstanceStorageManager;
import com.dianping.puma.taskexecutor.DefaultTaskExecutor;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/***
 * Abstract function test
 *
 * @author qi.yin
 */
public abstract class AbstractBaseDebug {

    private static final String dbConfigFile = "pumaIntegrationTest.properties";

    private static final String hostKey = "host";

    private static final String portKey = "port";

    private static final String usernameKey = "username";

    private static final String passwordKey = "password";

    private static final String serverIdKey = "serverId";

    private static File baseDir = new File(System.getProperty("java.io.tmpdir", "."), "puma");

    private static LocalFileInstanceStorageManager localFileInstanceStorageManager;

    private final int WAIT_FOR_SYNC_TIME = 2000;

    private RawEventCodec codec;

    private final String taskName = "test";

    private final String serverName = "testServer01";

    private TaskExecutor taskExecutor;

    protected static String host;

    protected static int port;

    protected static long serverId;

    protected static String username;

    protected static String password;

    protected static ComboPooledDataSource dataSource;

    protected static Connection connection;

    protected static QueryRunner queryRunner;

    protected static final String SCHEMA_NAME = "integrations";

    protected static String TABLE_NAME;

    @BeforeClass
    public static void beforeClass() throws IOException, SQLException {
        FileSystem.changeBasePath(baseDir.getAbsolutePath());
        initProperties();
        initDataSource();
        initSchema();
        initInstanceStorage();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        distroySchema();
        distroyDataSource();
    }

    @Before
    public void before() throws Exception {
        System.out.println();
        System.out.println("java.io.tmpdir is at " + System.getProperty("java.io.tmpdir"));
        codec = new RawEventCodec();
        startTask();
    }

    @After
    public void after() throws Exception {
        stopTask();
        distroyDirectory();
    }

    protected void test(TestLogic testLogic) throws Exception {
        testLogic.doLogic();
    }

    private static void initDataSource() {
        try {
            dataSource = new ComboPooledDataSource();
            dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/");
            dataSource.setUser(username);
            dataSource.setPassword(password);
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            queryRunner = new QueryRunner(dataSource);
        } catch (PropertyVetoException e) {
            throw new RuntimeException();
        }
    }

    private static void initSchema() throws SQLException {
        try {
            String dropSQL = "DROP SCHEMA IF EXISTS " + SCHEMA_NAME;

            queryRunner.update(dropSQL);

            String createSQL = "CREATE SCHEMA IF NOT EXISTS " + SCHEMA_NAME;

            queryRunner.update(createSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initProperties() throws IOException {
        Properties prop = new Properties();
        InputStream is = null;
        is = AbstractBaseDebug.class.getClassLoader().getResourceAsStream(dbConfigFile);
        try {
            prop.load(is);
            host = prop.getProperty(hostKey);
            port = Integer.parseInt(prop.getProperty(portKey));
            username = prop.getProperty(usernameKey);
            password = prop.getProperty(passwordKey);
            serverId = Long.parseLong(prop.getProperty(serverIdKey));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
                is = null;
            }
        }
    }

    private static void distroySchema() {
        try {
            String dropSQL = "DROP SCHEMA IF EXISTS " + SCHEMA_NAME;

            queryRunner.update(dropSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void distroyDataSource() {
        dataSource.close();
    }

    private static void distroyDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir", "."), "puma"));
    }

    private void startTask() throws Exception {
        taskExecutor = buildTask();
        taskExecutor.initContext();

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        taskExecutor.start();
                    } catch (Exception e) {
                    }
                }
            }).start();
        } catch (Exception e) {
            throw e;
        }
    }

    private void stopTask() throws Exception {
        if (taskExecutor != null) {
            try {
                taskExecutor.stop();
            } catch (Exception e) {
                throw e;
            }
        }

    }

    private TaskExecutor buildTask() throws Exception {
        DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();

        //srcdb
        final HashSet<SrcDbEntity> srcDB = Sets.newHashSet(
                new SrcDbEntity()
                        .setHost(host)
                        .setPort(port)
                        .setUsername(username)
                        .setPassword(password)
                        .setTags(Sets.newHashSet(SrcDbEntity.TAG_WRITE)));

        //instance
        InstanceManager instanceManager = new InstanceManager() {
            @Override
            public void init() {

            }

            @Override
            public Set<SrcDbEntity> getUrlByCluster(String clusterName) {
                return srcDB;
            }

            @Override
            public String getClusterByDb(String db) {
                return "localhost";
            }
        };
        taskExecutor.setInstanceManager(instanceManager);

        // state
        PumaTaskStateEntity taskState = new PumaTaskStateEntity();
        taskState.setTaskName(taskName);
        taskExecutor.setTaskState(taskState);

        // Base.
        taskExecutor.setTaskName(taskName);
        taskExecutor.setServerId(taskName.hashCode() + serverName.hashCode());

        // Bin log.
        taskExecutor.setInstanceStorageManager(localFileInstanceStorageManager);
        taskExecutor.setBinlogInfo(getLastestBinlog());
        // data source
        SrcDbEntity srcdb = new SrcDbEntity();
        srcdb.setServerId(serverId);
        srcdb.setHost(host);
        srcdb.setPort(port);
        srcdb.setUsername(username);
        srcdb.setPassword(password);

        //instance task
        InstanceTask instanceTask = new InstanceTask();
        DatabaseTask databaseTask = new DatabaseTask();
        databaseTask.setDatabase(SCHEMA_NAME);
        databaseTask.setTables(Lists.newArrayList(TABLE_NAME));
        instanceTask.getDatabaseTasks().add(databaseTask);
        taskExecutor.setInstanceTask(instanceTask);

        // tableset
        TableSet tableSet = new TableSet();
        tableSet.add(new Table(SCHEMA_NAME, TABLE_NAME));

        // Parser.
        Parser parser = new DefaultBinlogParser();
        // parser.start();
        taskExecutor.setParser(parser);

        // Handler.
        DefaultDataHandler dataHandler = new DefaultDataHandler();
        DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
        tableMetaInfo.setSrcDbEntity(srcdb);
        tableMetaInfo.setAcceptedTables(tableSet);

        dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
        taskExecutor.setDataHandler(dataHandler);
        taskExecutor.setTableMetaInfoFetcher(tableMetaInfo);

        EventFilterChain eventFilterChain = new DefaultEventFilterChain();
        List<EventFilter> eventFilterList = new ArrayList<EventFilter>();

        // DML event filter.
        DMLEventFilter dmlEventFilter = new DMLEventFilter();
        dmlEventFilter.setName(taskName);
        dmlEventFilter.setDml(true);
        dmlEventFilter.setAcceptedTables(tableSet);
        eventFilterList.add(dmlEventFilter);

        // DDL event filter.
        DDLEventFilter ddlEventFilter = new DDLEventFilter();
        ddlEventFilter.setName(taskName);
        ddlEventFilter.setDdl(true);
        List<DDLType> ddlTypes = new ArrayList<DDLType>();
        ddlTypes.add(DDLType.ALTER_TABLE);
        ddlTypes.add(DDLType.CREATE_INDEX);
        ddlTypes.add(DDLType.DROP_INDEX);
        ddlEventFilter.setDdlTypes(ddlTypes);
        eventFilterList.add(ddlEventFilter);

        // Transaction event filter.
        TransactionEventFilter transactionEventFilter = new TransactionEventFilter();
        transactionEventFilter.setName(taskName);
        transactionEventFilter.setBegin(true);
        transactionEventFilter.setCommit(true);
        eventFilterList.add(transactionEventFilter);

        eventFilterChain.setEventFilters(eventFilterList);

        // File sender.
        List<Sender> senders = new ArrayList<Sender>();
        FileDumpSender sender = new FileDumpSender();
        sender.setName("fileSender-" + taskName);
        sender.setStorageEventFilterChain(eventFilterChain);

        senders.add(sender);

        // Global Config
        //GlobalStorageConfig.BINLOG_INDEX_BASE_DIR = binlogIndexBaseDir.getAbsolutePath();
        //GlobalStorageConfig.MASTER_STORAGE_BASE_DIR = masterStorageBaseDir.getAbsolutePath();
        //GlobalStorageConfig.SLAVE_STORAGE_BASE_DIR = slaveStorageBaseDir.getAbsolutePath();

        // Dispatch.
        SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
        dispatcher.setName("dispatch-" + taskName);
        dispatcher.setSenders(senders);
        taskExecutor.setDispatcher(dispatcher);

        return taskExecutor;
    }

    protected static void setFilterTable(String tableName) {
        TABLE_NAME = tableName;
    }

    private static void initInstanceStorage() {
        localFileInstanceStorageManager = new LocalFileInstanceStorageManager();
        localFileInstanceStorageManager.init();
    }

    protected BinlogInfo getLastestBinlog() {
        Object[] objs = null;
        try {
            String querySQL = "SHOW MASTER STATUS";
            objs = queryRunner.query(querySQL, new ArrayHandler());
            if (objs == null) {
                throw new RuntimeException();
            }
            BinlogInfo binlogInfo = new BinlogInfo();
            binlogInfo.setBinlogFile(String.valueOf(objs[0]));
            binlogInfo.setBinlogPosition(Long.valueOf(String.valueOf(objs[1])));
            binlogInfo.setEventIndex(0);
            return binlogInfo;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    protected List<ChangedEvent> getEvents(int n, boolean needTs, boolean isRowChangedEvent, boolean isDdlEvent)
            throws Exception {
        waitForSync(WAIT_FOR_SYNC_TIME);
        List<ChangedEvent> result = new ArrayList<ChangedEvent>();

        ReadChannel readChannel = ChannelFactory.newReadChannel(SCHEMA_NAME);
        readChannel.start();
        readChannel.openOldest();

        for (int i = 0; i < n; ) {
            ChangedEvent event = (ChangedEvent) readChannel.next();
            if (!needTs) {
                if (event instanceof RowChangedEvent) {
                    if (((RowChangedEvent) event).isTransactionBegin() || ((RowChangedEvent) event).isTransactionCommit()) {
                        continue;
                    }
                }
            } else if (!isRowChangedEvent) {
                continue;
            } else if (!isDdlEvent) {
                continue;
            }
            i++;
            result.add(event);
        }
        readChannel.stop();
        return result;
    }

    protected List<ChangedEvent> getEvents(int n, long seq, long serverId, String binlog, long binlogPos,
                                           long timeStamp, boolean needTs) throws Exception {
        waitForSync(WAIT_FOR_SYNC_TIME);
        List<ChangedEvent> result = new ArrayList<ChangedEvent>();

        ReadChannel readChannel = ChannelFactory.newReadChannel(SCHEMA_NAME);
        readChannel.open(new BinlogInfo(serverId, binlog, binlogPos, 0, 0));

        for (int i = 0; i < n; ) {
            ChangedEvent event = (ChangedEvent) readChannel.next();
            if (!needTs) {
                if (event instanceof RowChangedEvent) {
                    if (((RowChangedEvent) event).isTransactionBegin() || ((RowChangedEvent) event).isTransactionCommit()) {
                        continue;
                    }
                }
            }
            i++;
            result.add(event);
        }
        readChannel.stop();
        return result;
    }

    protected void waitForSync(long ms) throws Exception {
        Thread.sleep(ms);
    }

    protected static interface TestLogic {
        public void doLogic() throws Exception;
    }
}
