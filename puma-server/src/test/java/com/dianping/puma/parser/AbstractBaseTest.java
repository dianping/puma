package com.dianping.puma.parser;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.filter.DDLEventFilter;
import com.dianping.puma.filter.DMLEventFilter;
import com.dianping.puma.filter.DefaultEventFilterChain;
import com.dianping.puma.filter.EventFilter;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.TransactionEventFilter;
import com.dianping.puma.parser.meta.DefaultTableMetaInfoFetcher;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatcherImpl;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.channel.DefaultEventChannel;
import com.dianping.puma.storage.conf.GlobalStorageConfig;
import com.dianping.puma.storage.holder.impl.DefaultBinlogInfoHolder;
import com.dianping.puma.taskexecutor.DefaultTaskExecutor;
import com.dianping.puma.taskexecutor.TaskExecutor;
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

	protected static final String SCHEMA_NAME = "Integrations";

	protected static String TABLE_NAME;
	
	@BeforeClass
	public static void beforeClass() throws IOException, SQLException {
		initProperties();
		initDataSource();
		initSchema();
		initbinlogHolder();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		distroySchema();
		distroyDataSource();
	}

	@Before
	public void before() throws Exception {
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
		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir", "."), "puma"));
	}

	private void startTask() throws Exception {
		taskExecutor = buildTask();
		taskExecutor.initContext();

		try {
			PumaThreadUtils.createThread(new Runnable() {
				@Override
				public void run() {
					try {
						taskExecutor.start();
					} catch (Exception e) {
						taskExecutor.getTaskState().setStatus(Status.FAILED);
					}
				}
			}, taskExecutor.getTaskName(), false).start();
		} catch (Exception e) {
			taskExecutor.getTaskState().setStatus(Status.FAILED);
			throw e;
		}

		taskExecutor.getTaskState().setStatus(Status.RUNNING);
	}

	private void stopTask() throws Exception {
		if (taskExecutor != null) {
			taskExecutor.getTaskState().setStatus(Status.STOPPING);

			try {
				taskExecutor.stop();
			} catch (Exception e) {
				taskExecutor.getTaskState().setStatus(Status.FAILED);
				throw e;
			}

			taskExecutor.getTaskState().setStatus(Status.STOPPED);

		}

	}

	private TaskExecutor buildTask() throws Exception {
		DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();

		// state
		PumaTaskStateEntity taskState = new PumaTaskStateEntity();
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
		SrcDbEntity srcdb = new SrcDbEntity();
		srcdb.setServerId(serverId);
		srcdb.setHost(host);
		srcdb.setPort(port);
		srcdb.setUsername(username);
		srcdb.setPassword(password);

		// task
		PumaTaskEntity task = new PumaTaskEntity();
		TableSet tableSet = new TableSet();
		tableSet.add(new Table(SCHEMA_NAME, TABLE_NAME));
		task.setTableSet(tableSet);
		Set<SrcDbEntity> enties = new HashSet<SrcDbEntity>();
		enties.add(srcdb);
		task.setSrcDbEntityList(enties);

		taskExecutor.setTask(task);

		// Parser.
		Parser parser = new DefaultBinlogParser();
		// parser.start();
		taskExecutor.setParser(parser);

		// Handler.
		DefaultDataHandler dataHandler = new DefaultDataHandler();
		DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
		tableMetaInfo.setSrcDbEntity(srcdb);
		tableMetaInfo.setAcceptedTables(task.getTableSet());

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
		sender.setTaskName(taskName);
		sender.setCodec(codec);
		sender.setStorageEventFilterChain(eventFilterChain);
		sender.setPreservedDay(2);

		senders.add(sender);

		// Global Config
		GlobalStorageConfig.binlogIndexBaseDir = binlogIndexBaseDir.getAbsolutePath();
		GlobalStorageConfig.masterStorageBaseDir = masterStorageBaseDir.getAbsolutePath();
		GlobalStorageConfig.slaveStorageBaseDir = slaveStorageBaseDir.getAbsolutePath();

		// Dispatch.
		SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
		dispatcher.setName("dispatch-" + taskName);
		dispatcher.setSenders(senders);
		taskExecutor.setDispatcher(dispatcher);

		// Set puma task status.
		taskExecutor.getTaskState().setStatus(Status.WAITING);

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
		waitForSync(WAIT_FOR_SYNC_TIME);
		List<ChangedEvent> result = new ArrayList<ChangedEvent>();

		EventChannel channel = new DefaultEventChannel(SCHEMA_NAME);
		channel.open(SubscribeConstant.SEQ_FROM_OLDEST);

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
		waitForSync(WAIT_FOR_SYNC_TIME);
		List<ChangedEvent> result = new ArrayList<ChangedEvent>();

		EventChannel channel = new DefaultEventChannel(SCHEMA_NAME);
		channel.open(serverId, binlog, binlogPos);

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
