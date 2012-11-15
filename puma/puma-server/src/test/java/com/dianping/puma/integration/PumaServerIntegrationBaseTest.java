/**
 * Project: puma-server
 * 
 * File Created at 2012-7-26
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.integration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.common.NotifyService;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatherImpl;
import com.dianping.puma.server.MMapBasedBinlogPositionHolder;
import com.dianping.puma.server.ReplicationBasedServer;
import com.dianping.puma.storage.ArchiveStrategy;
import com.dianping.puma.storage.BinlogIndexManager;
import com.dianping.puma.storage.BucketIndex;
import com.dianping.puma.storage.CleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.LocalFileBucketIndex;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * TODO Comment of PumaServerIntegrationTest
 * 
 * @author Leo Liang
 * 
 */
public abstract class PumaServerIntegrationBaseTest {
	private static final String			dbConfigFile			= "PumaServerIntegrationTest.properties";
	protected static MysqlDataSource	ds;
	protected static String				host;
	protected static int				port;
	private static String				pwd;
	private static String				user;
	protected static String				db;
	protected ReplicationBasedServer	server;
	protected DefaultEventStorage		storage;
	protected LocalFileBucketIndex		masterIndex;
	protected LocalFileBucketIndex		slaveIndex;
	protected BinlogIndexManager		binlogIndexManager;
	protected FileDumpSender			sender;
	private static File					storageMasterBaseDir	= new File(System.getProperty("java.io.tmpdir", "."),
																		"Puma");
	private static File					storageSlaveBaseDir		= new File(System.getProperty("java.io.tmpdir", "."),
																		"Puma/bak/");
	private static File					confBaseDir				= new File(System.getProperty("java.io.tmpdir", "."),
																		"PumaConf/");

	@BeforeClass
	public static void beforeClass() throws Exception {
		initProperties();
		initDataSource();
		FileUtils.deleteDirectory(confBaseDir);
		// createDB();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		// dropDB();
	}

	protected void startServer() throws Exception {
		NotifyService mockNotifyService = new NotifyService() {

			@Override
			public void report(String title, Map<String, Map<String, String>> msg) {
				System.out.println("report-----" + title + ": " + msg);
			}

			@Override
			public void alarm(String msg, Throwable t, boolean sendSms) {
				System.out.println("alarm-----" + msg + ": " + t);
			}
		};

		MMapBasedBinlogPositionHolder binlogPositionHolder = new MMapBasedBinlogPositionHolder();
		binlogPositionHolder.setBaseDir(confBaseDir.getAbsolutePath());
		binlogPositionHolder.init();

		// init parser
		Parser parser = new DefaultBinlogParser();
		parser.start();

		// init tablemetasinfofetcher
		DefaultTableMetaInfoFetcher tableMetaInfoFetcher = new DefaultTableMetaInfoFetcher();
		tableMetaInfoFetcher.setMetaDBHost(host);
		tableMetaInfoFetcher.setMetaDBPassword(pwd);
		tableMetaInfoFetcher.setMetaDBPort(port);
		tableMetaInfoFetcher.setMetaDBUser(user);

		// init dataHandler
		DefaultDataHandler dataHandler = new DefaultDataHandler();
		dataHandler.setTableMetasInfoFetcher(tableMetaInfoFetcher);
		dataHandler.setNotifyService(mockNotifyService);
		dataHandler.start();

		// init index
		masterIndex = new LocalFileBucketIndex();
		masterIndex.setBaseDir(storageMasterBaseDir.getAbsolutePath());
		masterIndex.setMaxBucketLengthMB(1);
		masterIndex.start();
		slaveIndex = new LocalFileBucketIndex();
		slaveIndex.setBaseDir(storageSlaveBaseDir.getAbsolutePath());
		slaveIndex.start();
		binlogIndexManager = new BinlogIndexManager();
		binlogIndexManager.setMainbinlogIndexFileName("binlogIndex");
		binlogIndexManager.setMainbinlogIndexFileNameBasedir(System.getProperty("java.io.tmpdir", ".") + "/Puma");
		binlogIndexManager.setSubBinlogIndexBaseDir(System.getProperty("java.io.tmpdir", ".") + "/binlogindex");
		binlogIndexManager.setSubBinlogIndexPrefix("index-");
		binlogIndexManager.setBucketFilePrefix("bucket-");
		binlogIndexManager.setCodec(new JsonEventCodec());

		// init storage
		storage = new DefaultEventStorage();
		storage.setCodec(new JsonEventCodec());
		storage.setArchiveStrategy(new ArchiveStrategy() {

			@Override
			public void archive(BucketIndex masterIndex, BucketIndex slaveIndex) {
			}
		});
		storage.setCleanupStrategy(new CleanupStrategy() {

			@Override
			public void cleanup(BucketIndex index, BinlogIndexManager binlogIndexManager) {

			}
		});

		storage.setName("test-storage");
		storage.setMasterIndex(masterIndex);
		storage.setSlaveIndex(slaveIndex);
		storage.setBinlogIndexManager(binlogIndexManager);
		storage.start();

		// init sender
		sender = new FileDumpSender();
		sender.setName("test-sender");
		sender.setStorage(storage);
		sender.setNotifyService(mockNotifyService);
		sender.start();

		// init dispatcher
		SimpleDispatherImpl dispatcher = new SimpleDispatherImpl();
		dispatcher.setName("test-dispatcher");
		dispatcher.setSenders(Arrays.asList(new Sender[] { sender }));

		// init server
		server = new ReplicationBasedServer();
		server.setDatabase(db);
		server.setServerId(System.currentTimeMillis());
		server.setEncoding("UTF-8");
		server.setUser(user);
		server.setPort(port);
		server.setHost(host);
		server.setPassword(pwd);
		BinlogInfo binlogInfo = getLatestBinlogInfo();
		server.setDefaultBinlogFileName(binlogInfo.getFile());
		server.setDefaultBinlogPosition(binlogInfo.getPos());
		server.setParser(parser);
		server.setDataHandler(dataHandler);
		server.setDispatcher(dispatcher);
		server.setNotifyService(mockNotifyService);
		server.setBinlogPositionHolder(binlogPositionHolder);

		PumaContext context = new PumaContext();

		context.setPumaServerId(server.getServerId());
		context.setPumaServerName(server.getServerName());
		context.setBinlogFileName(server.getDefaultBinlogFileName());
		context.setBinlogStartPos(server.getDefaultBinlogPosition());
		server.setContext(context);

		PumaThreadUtils.createThread(new Runnable() {

			@Override
			public void run() {
				try {
					server.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "ServerThread", false).start();
	}

	protected List<ChangedEvent> getEvents(int n, boolean needTs) throws Exception {
		waitForSync(50);
		List<ChangedEvent> result = new ArrayList<ChangedEvent>();
		EventChannel channel = storage.getChannel(-1, 1, null, null);
		for (int i = 0; i < n;) {
			ChangedEvent event = channel.next();
			if (!needTs) {
				if (event instanceof RowChangedEvent) {
					if (((RowChangedEvent) event).isTransactionBegin()
							|| ((RowChangedEvent) event).isTransactionCommit()) {
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

	protected void executeSql(String script) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			stmt.execute(script);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}

	protected void insertWithBinaryColumn(String script, byte[] data) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(script);
			pstmt.setBinaryStream(1, new ByteArrayInputStream(data), data.length);
			pstmt.execute();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}

	protected void executeSqlWithTransaction(List<String> scripts) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			for (String script : scripts) {
				stmt.execute(script);
			}
			conn.commit();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}

	protected void test(TestLogic logic) throws Exception {
		startServer();
		logic.doLogic();
	}

	@After
	public void after() throws Exception {
		stopServer();
		FileUtils.deleteDirectory(storageMasterBaseDir);
		FileUtils.deleteDirectory(storageSlaveBaseDir);

		doAfter();
	}

	protected void stopServer() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected abstract void doAfter() throws Exception;

	protected BinlogInfo getLatestBinlogInfo() throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW MASTER STATUS");
			if (!rs.next()) {
				throw new RuntimeException("Unexpected empty resultset.");
			}
			return new BinlogInfo(rs.getString("File"), rs.getLong("Position"));

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}

	protected static void initDataSource() throws Exception {
		ds = new MysqlDataSource();
		ds.setUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
		ds.setUser(user);
		ds.setPassword(pwd);
	}

	protected static void createDB() throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			stmt.execute("DROP DATABASE IF EXISTS " + db);
			stmt.execute("CREATE DATABASE " + db);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}

	protected static void dropDB() throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			stmt.execute("DROP DATABASE IF EXISTS" + db);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}

	protected MysqlDataSource getDataSource() {
		return ds;
	}

	private static void initProperties() throws Exception {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = PumaServerIntegrationBaseTest.class.getClassLoader().getResourceAsStream(dbConfigFile);
			prop.load(is);
			host = prop.getProperty("dbHost");
			port = Integer.valueOf(prop.getProperty("dbPort"));
			user = prop.getProperty("dbUser");
			pwd = prop.getProperty("dbPwd");
			db = prop.getProperty("dbName");
		} finally {
			if (is != null) {
				is.close();
			}
		}

	}

	private static class BinlogInfo {
		private String	file;
		private long	pos;

		/**
		 * @param file
		 * @param pos
		 */
		public BinlogInfo(String file, long pos) {
			super();
			this.file = file;
			this.pos = pos;
		}

		/**
		 * @return the file
		 */
		public String getFile() {
			return file;
		}

		/**
		 * @return the pos
		 */
		public long getPos() {
			return pos;
		}

	}

	protected static interface TestLogic {
		public void doLogic() throws Exception;
	}

}
