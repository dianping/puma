package com.dianping.puma.status;

import com.dianping.puma.core.model.BinlogInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SystemStatus {

	private Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

	private Map<String, Server> servers = new ConcurrentHashMap<String, Server>();

	private int storeQps;

	private long totalParsedEvent;

	private long totalStoreBytes;

	private long totalStoreCount;

	private long totalInsertEvent;

	private long totalUpdateEvent;

	private long totalDeleteEvent;

	private long totalDdlEvent;

	public Map<String, Client> getClients() {
		return clients;
	}

	public Map<String, Server> getServers() {
		return servers;
	}

	public int getStoreQps() {
		this.storeQps = 0;

		for (Server server : this.servers.values()) {
			this.storeQps += server.getStoreQps();
		}

		return storeQps;
	}

	public long getTotalDdlEvent() {
		this.totalDdlEvent = 0L;

		for (Server server : this.servers.values()) {
			this.totalDdlEvent += server.getTotalDdlEvent().get();
		}

		return totalDdlEvent;
	}

	public long getTotalDeleteEvent() {
		this.totalDeleteEvent = 0L;

		for (Server server : this.servers.values()) {
			this.totalDeleteEvent += server.getTotalDeleteEvent().get();
		}

		return totalDeleteEvent;
	}

	public long getTotalInsertEvent() {
		this.totalInsertEvent = 0L;

		for (Server server : this.servers.values()) {
			this.totalInsertEvent += server.getTotalInsertEvent().get();
		}

		return totalInsertEvent;
	}

	public long getTotalParsedEvent() {
		this.totalParsedEvent = 0L;

		for (Server server : this.servers.values()) {
			this.totalParsedEvent += server.getTotalParsedEvent().get();
		}

		return totalParsedEvent;
	}

	public long getTotalStoreBytes() {
		this.totalStoreBytes = 0L;

		for (Server server : this.servers.values()) {
			this.totalStoreBytes += server.getTotalStoreBytes().get();
		}

		return totalStoreBytes;
	}

	public long getTotalStoreCount() {
		this.totalStoreCount = 0L;

		for (Server server : this.servers.values()) {
			this.totalStoreCount += server.getTotalStoreCount().get();
		}

		return totalStoreCount;
	}

	public long getTotalUpdateEvent() {
		this.totalUpdateEvent = 0L;

		for (Server server : this.servers.values()) {
			this.totalUpdateEvent += server.getTotalUpdateEvent().get();
		}

		return totalUpdateEvent;
	}

	public void setClients(Map<String, Client> clients) {
		this.clients = clients;
	}

	public void setServers(Map<String, Server> servers) {
		this.servers = servers;
	}

	public void setStoreQps(int storeQps) {
		this.storeQps = storeQps;
	}

	public static class Client {
		private String ip;

		private String database;

		private List<String> tables;

		private boolean withDml;

		private boolean withDdl;

		private boolean withTransaction;

		private String codec;

		private BinlogInfo sendBinlogInfo;

		private BinlogInfo ackBinlogInfo;

		private int fetchQps;

		private SecondBucketCounter fetchQpsCounter;

		public Client(String ip, String database, List<String> tables, boolean withDml, boolean withDdl,
		      boolean withTransaction, String codec) {
			super();
			this.ip = ip;
			this.database = database;
			this.tables = tables;
			this.withDml = withDml;
			this.withDdl = withDdl;
			this.withTransaction = withTransaction;
			this.codec = codec;
			this.fetchQpsCounter = new SecondBucketCounter();
		}

		public String getCodec() {
			return codec;
		}

		public String getDatabase() {
			return database;
		}

		public int getFetchQps() {
			this.fetchQps = this.fetchQpsCounter.get();

			return fetchQps;
		}

		public String getIp() {
			return ip;
		}

		public List<String> getTables() {
			return tables;
		}

		public boolean isWithDdl() {
			return withDdl;
		}

		public boolean isWithDml() {
			return withDml;
		}

		public boolean isWithTransaction() {
			return withTransaction;
		}

		public BinlogInfo getAckBinlogInfo() {
			return ackBinlogInfo;
		}

		public void setAckBinlogInfo(BinlogInfo ackBinlogInfo) {
			this.ackBinlogInfo = ackBinlogInfo;
		}

		public BinlogInfo getSendBinlogInfo() {
			return sendBinlogInfo;
		}

		public void setSendBinlogInfo(BinlogInfo sendBinlogInfo) {
			this.sendBinlogInfo = sendBinlogInfo;
		}

		public void setFetchQps(int fetchQps) {
			this.fetchQps = fetchQps;
		}
	}

	public static class Server {
		private String name;

		private String host;

		private int port;

		private String database;

		private String binlogFile;

		private long binlogPosition = 0L;

		private int bucketDate;

		private int bucketNumber;

		private int storeQps;

		private AtomicLong totalParsedEvent = new AtomicLong(0);

		private AtomicLong totalStoreCount = new AtomicLong(0);

		private AtomicLong totalStoreBytes = new AtomicLong(0);

		private AtomicLong totalInsertEvent = new AtomicLong(0);

		private AtomicLong totalUpdateEvent = new AtomicLong(0);

		private AtomicLong totalDeleteEvent = new AtomicLong(0);

		private AtomicLong totalDdlEvent = new AtomicLong(0);

		private SecondBucketCounter storeQpsCounter;

		public Server(String name, String host, int port) {
			this(name, host, port, null);
		}

		public Server(String name, String host, int port, String database) {
			super();
			this.name = name;
			this.host = host;
			this.port = port;
			this.database = database;
			this.storeQpsCounter = new SecondBucketCounter();
		}

		public String getBinlogFile() {
			return binlogFile;
		}

		public long getBinlogPosition() {
			return binlogPosition;
		}

		public int getBucketDate() {
			return bucketDate;
		}

		public int getBucketNumber() {
			return bucketNumber;
		}

		public String getDatabase() {
			return database;
		}

		public String getHost() {
			return host;
		}

		public String getName() {
			return name;
		}

		public int getPort() {
			return port;
		}

		public int getStoreQps() {
			this.storeQps = this.storeQpsCounter.get();

			return storeQps;
		}

		public AtomicLong getTotalDdlEvent() {
			return totalDdlEvent;
		}

		public AtomicLong getTotalDeleteEvent() {
			return totalDeleteEvent;
		}

		public AtomicLong getTotalInsertEvent() {
			return totalInsertEvent;
		}

		public AtomicLong getTotalParsedEvent() {
			return totalParsedEvent;
		}

		public AtomicLong getTotalStoreBytes() {
			return totalStoreBytes;
		}

		public AtomicLong getTotalStoreCount() {
			return totalStoreCount;
		}

		public AtomicLong getTotalUpdateEvent() {
			return totalUpdateEvent;
		}

		public void incStoreCountAndByte(long size) {
			this.totalStoreCount.incrementAndGet();
			this.totalStoreBytes.addAndGet(size);
			this.storeQpsCounter.increase();
		}

		public void setBinlogFile(String binlogFile) {
			this.binlogFile = binlogFile;
		}

		public void setBinlogPosition(long binlogPosition) {
			this.binlogPosition = binlogPosition;
		}

		public void setBucketDate(int bucketDate) {
			this.bucketDate = bucketDate;
		}

		public void setBucketNumber(int bucketOffset) {
			this.bucketNumber = bucketOffset;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public void setStoreQps(int storeQps) {
			this.storeQps = storeQps;
		}

		public void updateBinlog(String binlogFile, long binlogPosition) {
			this.binlogFile = binlogFile;
			this.binlogPosition = binlogPosition;
		}

		public void updateBucket(int bucketDate, int bucketNumber) {
			this.bucketDate = bucketDate;
			this.bucketNumber = bucketNumber;
		}
	}
}
