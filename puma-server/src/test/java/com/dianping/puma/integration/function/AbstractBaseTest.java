package com.dianping.puma.integration.function;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.dianping.puma.biz.entity.SrcDBInstance;
import com.dianping.puma.codec.RawEventCodec;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.core.model.event.AcceptedTableChangedEvent;
import com.dianping.puma.core.model.event.EventCenter;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.core.storage.holder.impl.DefaultBinlogInfoHolder;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.filter.DDLEventFilter;
import com.dianping.puma.filter.DMLEventFilter;
import com.dianping.puma.filter.DefaultEventFilterChain;
import com.dianping.puma.filter.EventFilter;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.TableMetaRefreshFilter;
import com.dianping.puma.filter.TransactionEventFilter;
import com.dianping.puma.meta.DefaultTableMetaInfoFectcher;
import com.dianping.puma.meta.TableMetaInfoStore;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatcherImpl;
import com.dianping.puma.server.DefaultTaskExecutor;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.LocalFileBucketIndex;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/***
 * Abstract function test
 *
 * @author qi.yin
 */
public abstract class AbstractBaseTest {

	private static final String dbConfigFile = "pumaIntegrationTest.properties";

	private static final String hostKey = "host";

	private static final String portKey = "port";

	private static final String usernameKey = "username";

	private static final String passwordKey = "password";

	private static final String serverIdKey = "serverId";

	private static File baseDir = new File(System.getProperty("java.io.tmpdir", "."), "puma/binlog/");

	private static File bakBaseDir = new File(System.getProperty("java.io.tmpdir", "."), "puma/binlog/bak/");

	private static final File masterStorageBaseDir = new File(System.getProperty("java.io.tmpdir", "."),
	      "puma/storage/master");

	private static final File slaveStorageBaseDir = new File(System.getProperty("java.io.tmpdir", "."),
	      "puma/storage/slave/");

	private static final File storageBakBaseDir = new File(System.getProperty("java.io.tmpdir", "."), "puma/bak/");

	private static final File binlogIndexBaseDir = new File(System.getProperty("java.io.tmpdir", "."),
	      "puma/binlogIndex/");

	private static DefaultBinlogInfoHolder binlogInfoHolder;

	private EventCenter eventCenter;

	private RawEventCodec codec;

	private final String taskName = "test";

	private final String serverName = "testServer01";

	private TaskExecutor taskExecutor;

	private DefaultEventStorage storage;

	protected static String host;

	protected static int port;

	protected static long serverId;

	protected static String username;

	protected static String password;

	protected static ComboPooledDataSource dataSource;

	protected static Connection connection;

	protected static QueryRunner queryRunner;

	protected static final String SCHEMA_NAME = "Integrations";

	protected static String TABLE_NAME;

	@BeforeClass
	public static void beforeClass() throws IOException, SQLException {
		initProperties();
		initDataSource();
		initSchema();
	}

//	@AfterClass
//	public static void afterClass() throws IOException {
//		distroySchema();
//		distroyDataSource();
//	}

	@Before
	public void before() throws Exception {
		initbinlogHolder();
		eventCenter = new EventCenter();
		eventCenter.init();
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
		is = AbstractBaseTest.class.getClassLoader().getResourceAsStream(dbConfigFile);
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
		FileUtils.deleteDirectory(baseDir);
		FileUtils.deleteDirectory(masterStorageBaseDir);
		FileUtils.deleteDirectory(slaveStorageBaseDir);
		FileUtils.deleteDirectory(storageBakBaseDir);
		FileUtils.deleteDirectory(binlogIndexBaseDir);
	}

	private void startTask() throws Exception {

		taskExecutor = buildTask();
		taskExecutor.initContext();

		AcceptedTableChangedEvent acceptedTableChangedEvent = new AcceptedTableChangedEvent();
		acceptedTableChangedEvent.setName(taskName);
		TableSet tableSet = new TableSet();
		Table table = new Table();
		table.setSchemaName(SCHEMA_NAME);
		table.setTableName(TABLE_NAME);
		tableSet.add(table);
		acceptedTableChangedEvent.setTableSet(tableSet);
		eventCenter.post(acceptedTableChangedEvent);

		try {
			PumaThreadUtils.createThread(new Runnable() {
				@Override
				public void run() {
					try {
						taskExecutor.start();
					} catch (Exception e) {
						taskExecutor.setStatus(Status.FAILED);
					}
				}
			}, taskExecutor.getTaskName(), false).start();
		} catch (Exception e) {
			taskExecutor.setStatus(Status.FAILED);
			throw e;
		}

		taskExecutor.setStatus(Status.RUNNING);
	}

	private void stopTask() throws Exception {
		if (taskExecutor != null) {
			taskExecutor.setStatus(Status.STOPPING);

			try {
				taskExecutor.stop();
			} catch (Exception e) {
				taskExecutor.setStatus(Status.FAILED);
				throw e;
			}

			taskExecutor.setStatus(Status.STOPPED);

		}

	}

	private TaskExecutor buildTask() throws Exception {
		DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();

		// state
		PumaTaskState taskState = new PumaTaskState();
		taskState.setName(taskName + "&" + serverName);
		taskState.setServerName(serverName);
		taskState.setTaskName(taskName);
		taskState.setStatus(Status.PREPARING);
		taskExecutor.setTaskState(taskState);

		// Base.
		taskExecutor.setTaskName(taskName);
		taskExecutor.setServerId(taskName.hashCode() + serverName.hashCode());

		// Bin log.
		taskExecutor.setBinlogInfoHolder(binlogInfoHolder);
		taskExecutor.setBinlogInfo(getLastestBinlog());
		taskExecutor.setBinlogStat(new BinlogStat());
		// data source
		taskExecutor.setDbServerId(serverId);
		taskExecutor.setDBHost(host);
		taskExecutor.setPort(port);
		taskExecutor.setDBUsername(username);
		taskExecutor.setDBPassword(password);
		// Parser.
		Parser parser = new DefaultBinlogParser();
		// parser.start();
		taskExecutor.setParser(parser);

		// Handler.
		DefaultDataHandler dataHandler = new DefaultDataHandler();
		DefaultTableMetaInfoFectcher tableMetaInfo = new DefaultTableMetaInfoFectcher();
		// tableMetaInfo.setAcceptedDataTables(pumaTask.getAcceptedDataInfos());
		SrcDBInstance srcDbInstance = new SrcDBInstance();
		srcDbInstance.setHost(host);
		srcDbInstance.setPassword(password);
		srcDbInstance.setPort(port);
		srcDbInstance.setUsername(username);
		srcDbInstance.setServerId(serverId);
		tableMetaInfo.setSrcDbInstance(srcDbInstance);
		BinlogInfo binlogInfo1 = new BinlogInfo("mysql-bin.000000", 4L);
		tableMetaInfo.setBinlogInfo(binlogInfo1);
		TableMetaInfoStore metaStore = new TableMetaInfoStore();
		metaStore.start();
		
		tableMetaInfo.setTableMetaInfoStore(metaStore);
		// tableMeta refresh filter
		TableMetaRefreshFilter tableMetaRefreshFilter = new TableMetaRefreshFilter();
		tableMetaRefreshFilter.setName(taskName);
		eventCenter.register(tableMetaRefreshFilter);
		tableMetaInfo.setTableMetaRefreshFilter(tableMetaRefreshFilter);

		dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
		dataHandler.start();
		taskExecutor.setDataHandler(dataHandler);

		// File sender.
		List<Sender> senders = new ArrayList<Sender>();
		FileDumpSender sender = new FileDumpSender();
		sender.setName("fileSender-" + taskName);

		// File sender storage.
		storage = new DefaultEventStorage();
		storage.setName("storage-" + taskName);
		storage.setTaskName(taskName);

		// storage.setAcceptedDataTables(pumaTask.getAcceptedDataInfos());
		storage.setCodec(codec);

		EventFilterChain eventFilterChain = new DefaultEventFilterChain();
		List<EventFilter> eventFilterList = new ArrayList<EventFilter>();

		// DML event filter.
		DMLEventFilter dmlEventFilter = new DMLEventFilter();
		dmlEventFilter.setName(taskName);
		dmlEventFilter.setDml(true);
		eventCenter.register(dmlEventFilter);
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
		eventCenter.register(ddlEventFilter);
		eventFilterList.add(ddlEventFilter);

		// Transaction event filter.
		TransactionEventFilter transactionEventFilter = new TransactionEventFilter();
		transactionEventFilter.setName(taskName);
		transactionEventFilter.setBegin(true);
		transactionEventFilter.setCommit(true);
		eventCenter.register(transactionEventFilter);
		eventFilterList.add(transactionEventFilter);

		eventFilterChain.setEventFilters(eventFilterList);
		storage.setStorageEventFilterChain(eventFilterChain);

		BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(taskName);
		if (binlogInfo != null) {
			storage.setBinlogInfo(binlogInfo);
		} else {
			storage.setBinlogInfo(taskExecutor.getBinlogInfo());
		}

		// File sender master storage.
		LocalFileBucketIndex masterBucketIndex = new LocalFileBucketIndex();
		masterBucketIndex.setBaseDir(masterStorageBaseDir.getAbsolutePath());
		masterBucketIndex.setBucketFilePrefix("Bucket-");
		masterBucketIndex.setMaxBucketLengthMB(1000);
		// masterBucketIndex.start();
		storage.setMasterBucketIndex(masterBucketIndex);

		// File sender slave storage.
		LocalFileBucketIndex slaveBucketIndex = new LocalFileBucketIndex();
		slaveBucketIndex.setBaseDir(slaveStorageBaseDir.getAbsolutePath());
		slaveBucketIndex.setBucketFilePrefix("Bucket-");
		slaveBucketIndex.setMaxBucketLengthMB(1000);
		// slaveBucketIndex.start();
		storage.setSlaveBucketIndex(slaveBucketIndex);

		// Archive strategy.
		DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
		archiveStrategy.setServerName(taskName);
		archiveStrategy.setMaxMasterFileCount(25);
		storage.setArchiveStrategy(archiveStrategy);

		// Clean up strategy.
		DefaultCleanupStrategy cleanupStrategy = new DefaultCleanupStrategy();
		cleanupStrategy.setPreservedDay(2);
		storage.setCleanupStrategy(cleanupStrategy);

		storage.setBinlogIndexBaseDir(binlogIndexBaseDir.getAbsolutePath());
		// storage.start();
		sender.setStorage(storage);
		// sender.start();
		senders.add(sender);

		// Dispatch.
		SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
		dispatcher.setName("dispatch-" + taskName);
		dispatcher.setSenders(senders);
		// dispatcher.start();
		taskExecutor.setDispatcher(dispatcher);

		// Set puma task status.
		taskExecutor.setStatus(Status.WAITING);

		return taskExecutor;
	}

	protected static void setFilterTable(String tableName) {
		TABLE_NAME = tableName;
	}

	private static void initbinlogHolder() {
		binlogInfoHolder = new DefaultBinlogInfoHolder();
		binlogInfoHolder.setBaseDir(baseDir.getAbsolutePath());
		binlogInfoHolder.setBakDir(bakBaseDir.getAbsolutePath());
		binlogInfoHolder.setBinlogIndexBaseDir(binlogIndexBaseDir.getAbsolutePath());
		binlogInfoHolder.setMasterStorageBaseDir(masterStorageBaseDir.getAbsolutePath());
		binlogInfoHolder.setSlaveStorageBaseDir(slaveStorageBaseDir.getAbsolutePath());
		binlogInfoHolder.setStorageBakDir(storageBakBaseDir.getAbsolutePath());
		binlogInfoHolder.init();
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
		waitForSync(3000);
		List<ChangedEvent> result = new ArrayList<ChangedEvent>();
		EventChannel channel = storage.getChannel(-1, -1, null, -1, -1);
		for (int i = 0; i < n;) {
			ChangedEvent event = (ChangedEvent) channel.next();
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
		channel.close();
		return result;
	}

	protected List<ChangedEvent> getEvents(int n, long seq, long serverId, String binlog, long binlogPos,
	      long timeStamp, boolean needTs) throws Exception {
		waitForSync(2000);
		List<ChangedEvent> result = new ArrayList<ChangedEvent>();
		EventChannel channel = storage.getChannel(seq, serverId, binlog, binlogPos, timeStamp);
		for (int i = 0; i < n;) {
			ChangedEvent event = (ChangedEvent) channel.next();
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
		channel.close();
		return result;
	}

	protected void waitForSync(long ms) throws Exception {
		Thread.sleep(ms);
	}

	protected static interface TestLogic {
		public void doLogic() throws Exception;
	}
}
