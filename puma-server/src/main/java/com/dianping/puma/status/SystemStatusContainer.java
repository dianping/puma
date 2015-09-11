///**
// * Project: puma-server
// * 
// * File Created at 2012-7-20
// * $Id$
// * 
// * Copyright 2010 dianping.com.
// * All rights reserved.
// *
// * This software is the confidential and proprietary information of
// * Dianping Company. ("Confidential Information").  You shall not
// * disclose such Confidential Information and shall use it only in
// * accordance with the terms of the license agreement you entered into
// * with dianping.com.
// */
//package com.dianping.puma.status;
//
//import java.util.Collections;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//
//import com.dianping.cat.Cat;
//import com.dianping.puma.core.model.BinlogInfo;
//
///**
// * @author Leo Liang
// * 
// */
//public enum SystemStatusContainer {
//	instance;
//
//	private Map<String, ServerStatus> serverStatus = new ConcurrentHashMap<String, ServerStatus>();
//
//	private Map<String, ClientStatus> clientStatus = new ConcurrentHashMap<String, ClientStatus>();
//
//	private Map<String, Long> clientSuccessSeq = new ConcurrentHashMap<String, Long>();
//
//	private Map<String, Long> storageStatus = new ConcurrentHashMap<String, Long>();
//
//	private ConcurrentMap<String, AtomicLong> serverParsedRowUpdateCount = new ConcurrentHashMap<String, AtomicLong>();
//
//	private ConcurrentMap<String, AtomicLong> serverParsedRowDeleteCount = new ConcurrentHashMap<String, AtomicLong>();
//
//	private ConcurrentMap<String, AtomicLong> serverParsedRowInsertCount = new ConcurrentHashMap<String, AtomicLong>();
//
//	private ConcurrentMap<String, AtomicLong> serverParsedDdlCount = new ConcurrentHashMap<String, AtomicLong>();
//
//	private ConcurrentMap<String, AtomicBoolean> stopTheWorlds = new ConcurrentHashMap<String, AtomicBoolean>();
//
//	private ConcurrentMap<String, AtomicInteger> metrics = new ConcurrentHashMap<String, AtomicInteger>();
//
//	private static final String CAT_KEY_EVENT_PARSED = "EventParsed-";
//
//	public void updateServerStatus(String name, String host, int port, String db, String binlogFile, long binlogPos) {
//		serverStatus.put(name, new ServerStatus(binlogFile, binlogPos, host, port, db));
//	}
//
//	public void removeAll(String taskName) {
//		serverStatus.remove(taskName);
//		clientStatus.remove(taskName);
//		clientSuccessSeq.remove(taskName);
//		storageStatus.remove("storage-" + taskName);
//		serverParsedRowInsertCount.remove(taskName);
//		serverParsedRowUpdateCount.remove(taskName);
//		serverParsedRowDeleteCount.remove(taskName);
//		serverParsedDdlCount.remove(taskName);
//		stopTheWorlds.remove(taskName);
//		metrics.remove(taskName);
//	}
//
//	private void logMetricForCount(String name) {
//		metrics.putIfAbsent(name, new AtomicInteger(0));
//		int count = metrics.get(name).incrementAndGet();
//		if (count != 0 && count % 100 == 0) {
//			metrics.get(name).getAndAdd(-100);
//			Cat.logMetricForCount(name, 100);
//		}
//	}
//
//	public void incServerRowUpdateCounter(String name) {
//		serverParsedRowUpdateCount.putIfAbsent(name, new AtomicLong(0));
//		serverParsedRowUpdateCount.get(name).incrementAndGet();
//		logMetricForCount(CAT_KEY_EVENT_PARSED + name);
//	}
//
//	public void incServerRowDeleteCounter(String name) {
//		serverParsedRowDeleteCount.putIfAbsent(name, new AtomicLong(0));
//		serverParsedRowDeleteCount.get(name).incrementAndGet();
//		logMetricForCount(CAT_KEY_EVENT_PARSED + name);
//	}
//
//	public void incServerRowInsertCounter(String name) {
//		serverParsedRowInsertCount.putIfAbsent(name, new AtomicLong(0));
//		serverParsedRowInsertCount.get(name).incrementAndGet();
//		logMetricForCount(CAT_KEY_EVENT_PARSED + name);
//	}
//
//	public void incServerDdlCounter(String name) {
//		serverParsedDdlCount.putIfAbsent(name, new AtomicLong(0));
//		serverParsedDdlCount.get(name).incrementAndGet();
//		logMetricForCount(CAT_KEY_EVENT_PARSED + name);
//	}
//
//	public void addClientStatus(String name, String ip, long seq, String target, boolean dml, boolean ddl, boolean ts,
//			String[] dt, String codec) {
//		clientStatus.put(name, new ClientStatus(target, dml, ddl, ts, codec, dt, seq, ip));
//	}
//
//	public void updateClientInfo(String name, long seq, BinlogInfo binlogInfo) {
//		if (clientStatus.get(name) != null) {
//			clientStatus.get(name).setSeq(seq);
//			clientSuccessSeq.put(name, seq);
//			updateClientBinlog(name,binlogInfo);
//			logMetricForCount("ClientConsumed-" + name);
//		}
//	}
//
//	public void updateClientBinlog(String name, String binlogFile,long binlogPos,int eventIndex) {
//		if (clientStatus.get(name) != null) {
//			if (clientStatus.get(name).getBinlogInfo() == null) {
//				clientStatus.get(name).setBinlogInfo(new BinlogInfo());
//			}
//			clientStatus.get(name).getBinlogInfo().setBinlogFile(binlogFile);
//			clientStatus.get(name).getBinlogInfo().setBinlogPosition(binlogPos);
//			clientStatus.get(name).getBinlogInfo().setEventIndex((eventIndex));
//		}
//	}
//	
//	public void updateClientBinlog(String name, BinlogInfo binlogInfo) {
//		updateClientBinlog(name,binlogInfo.getBinlogFile(),binlogInfo.getBinlogPosition(),binlogInfo.getEventIndex());
//	}
//
//	public void removeClient(String name) {
//		clientStatus.remove(name);
//	}
//
//	public void updateStorageStatus(String name, long seq) {
//		storageStatus.put(name, seq);
//		logMetricForCount("StorageStored-" + name);
//	}
//
//	public ServerStatus getServerStatus(String name) {
//		return serverStatus.get(name);
//	}
//
//	public ClientStatus getClientStatus(String name) {
//		return clientStatus.get(name);
//	}
//
//	public Long getClientSuccessSeq(String name) {
//		return clientSuccessSeq.get(name);
//	}
//
//	public long getStorageStatus(String name) {
//		return storageStatus.get(name);
//	}
//
//	public Map<String, ServerStatus> listServerStatus() {
//		return Collections.unmodifiableMap(serverStatus);
//	}
//
//	public Map<String, ClientStatus> listClientStatus() {
//		return Collections.unmodifiableMap(clientStatus);
//	}
//
//	public Map<String, Long> listClientSuccessSeq() {
//		return Collections.unmodifiableMap(clientSuccessSeq);
//	}
//
//	public AtomicLong getServerRowUpdateCounter(String taskId) {
//		return serverParsedRowUpdateCount.get(taskId);
//	}
//
//	public AtomicLong getServerRowDeleteCounter(String taskId) {
//		return serverParsedRowDeleteCount.get(taskId);
//	}
//
//	public AtomicLong getServerRowInsertCounter(String taskId) {
//		return serverParsedRowInsertCount.get(taskId);
//	}
//
//	public AtomicLong getServerDdlCounter(String taskId) {
//		return serverParsedDdlCount.get(taskId);
//	}
//
//	public Map<String, AtomicLong> listServerRowUpdateCounters() {
//		return Collections.unmodifiableMap(serverParsedRowUpdateCount);
//	}
//
//	public Map<String, AtomicLong> listServerRowDeleteCounters() {
//		return Collections.unmodifiableMap(serverParsedRowDeleteCount);
//	}
//
//	public Map<String, AtomicLong> listServerRowInsertCounters() {
//		return Collections.unmodifiableMap(serverParsedRowInsertCount);
//	}
//
//	public Map<String, AtomicLong> listServerDdlCounters() {
//		return Collections.unmodifiableMap(serverParsedDdlCount);
//	}
//
//	public Map<String, Long> listStorageStatus() {
//		return Collections.unmodifiableMap(storageStatus);
//	}
//
//	public static class ClientStatus {
//		private String target;
//
//		private boolean dml;
//
//		private boolean ddl;
//
//		private boolean needTsInfo;
//
//		private String codec;
//
//		private String[] dt;
//
//		private long seq;
//
//		private String ip;
//
//		private BinlogInfo binlogInfo;
//
//		public ClientStatus(String target, boolean dml, boolean ddl, boolean needTsInfo, String codec, String[] dt,
//				long seq, String ip) {
//			super();
//			this.target = target;
//			this.dml = dml;
//			this.ddl = ddl;
//			this.needTsInfo = needTsInfo;
//			this.codec = codec;
//			this.dt = dt;
//			this.seq = seq;
//			this.ip = ip;
//			binlogInfo = new BinlogInfo();
//		}
//
//		/**
//		 * @param seq
//		 *            the seq to set
//		 */
//		public void setSeq(long seq) {
//			this.seq = seq;
//		}
//
//		/**
//		 * @return the seq
//		 */
//		public long getSeq() {
//			return seq;
//		}
//
//		public void setIp(String ip) {
//			this.ip = ip;
//		}
//
//		public String getIp() {
//			return ip;
//		}
//
//		/**
//		 * @return the target
//		 */
//		public String getTarget() {
//			return target;
//		}
//
//		/**
//		 * @return the dml
//		 */
//		public boolean isDml() {
//			return dml;
//		}
//
//		/**
//		 * @return the ddl
//		 */
//		public boolean isDdl() {
//			return ddl;
//		}
//
//		/**
//		 * @return the needTsInfo
//		 */
//		public boolean isNeedTsInfo() {
//			return needTsInfo;
//		}
//
//		/**
//		 * @return the codec
//		 */
//		public String getCodec() {
//			return codec;
//		}
//
//		/**
//		 * @return the dt
//		 */
//		public String[] getDt() {
//			return dt;
//		}
//
//		public BinlogInfo getBinlogInfo() {
//			return binlogInfo;
//		}
//
//		public void setBinlogInfo(BinlogInfo binlogInfo) {
//			this.binlogInfo = binlogInfo;
//		}
//
//	}
//
//	public static class ServerStatus {
//
//		private String binlogFile;
//
//		private long binlogPos;
//
//		private String host;
//
//		private int port;
//
//		private String db;
//
//		public ServerStatus(String binlogFile, long binlogPos, String host, int port, String db) {
//			this.binlogFile = binlogFile;
//			this.binlogPos = binlogPos;
//			this.host = host;
//			this.port = port;
//			this.db = db;
//		}
//
//		/**
//		 * @return the host
//		 */
//		public String getHost() {
//			return host;
//		}
//
//		/**
//		 * @return the port
//		 */
//		public int getPort() {
//			return port;
//		}
//
//		/**
//		 * @return the db
//		 */
//		public String getDb() {
//			return db;
//		}
//
//		/**
//		 * @return the binlogFile
//		 */
//		public String getBinlogFile() {
//			return binlogFile;
//		}
//
//		/**
//		 * @return the binlogPos
//		 */
//		public long getBinlogPos() {
//			return binlogPos;
//		}
//
//	}
//
//	public boolean isStopTheWorld(String serverName) {
//		if (serverName == null) {
//			return false;
//		}
//		AtomicBoolean stopTheWorld = stopTheWorlds.get(serverName);
//		return (stopTheWorld != null) ? stopTheWorld.get() : false;
//	}
//
//	public void stopTheWorld(String serverName) {
//		stopTheWorlds.put(serverName, new AtomicBoolean(true));
//	}
//
//	public void startTheWorld(String serverName) {
//		stopTheWorlds.put(serverName, new AtomicBoolean(false));
//	}
//
//}
