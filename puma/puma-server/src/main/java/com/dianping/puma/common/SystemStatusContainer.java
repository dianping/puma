/**
 * Project: puma-server
 * 
 * File Created at 2012-7-20
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
package com.dianping.puma.common;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Leo Liang
 * 
 */
public enum SystemStatusContainer {
	instance;

	private Map<String, ServerStatus>			serverStatus			= new ConcurrentHashMap<String, ServerStatus>();
	private Map<String, ClientStatus>			clientStatus			= new ConcurrentHashMap<String, ClientStatus>();
	private Map<String, Long>					storageStatus			= new ConcurrentHashMap<String, Long>();
	private ConcurrentMap<String, AtomicLong>	serverParsedEventCount	= new ConcurrentHashMap<String, AtomicLong>();
	private ConcurrentMap<String, AtomicLong>	serverParsedRowCount		= new ConcurrentHashMap<String, AtomicLong>();

	public void updateServerStatus(String name, String host, int port, String db, String binlogFile, long binlogPos) {
		serverStatus.put(name, new ServerStatus(binlogFile, binlogPos, host, port, db));
	}

	public void incServerEventCounte(String name) {
		serverParsedEventCount.putIfAbsent(name, new AtomicLong(0));
		serverParsedEventCount.get(name).incrementAndGet();
	}

	public void incServerRowCounte(String name) {
		serverParsedRowCount.putIfAbsent(name, new AtomicLong(0));
		serverParsedRowCount.get(name).incrementAndGet();
	}

	public void addClientStatus(String name, long seq, String target, boolean dml, boolean ddl, boolean ts,
			String[] dt, String codec) {
		clientStatus.put(name, new ClientStatus(target, dml, ddl, ts, codec, dt, seq));
	}

	public void updateClientSeq(String name, long seq) {
		clientStatus.get(name).setSeq(seq);
	}

	public void removeClient(String name) {
		clientStatus.remove(name);
	}

	public void updateStorageStatus(String name, long seq) {
		storageStatus.put(name, seq);
	}

	public ServerStatus getServerStatus(String name) {
		return serverStatus.get(name);
	}

	public ClientStatus getClientStatus(String name) {
		return clientStatus.get(name);
	}

	public long getStorageStatus(String name) {
		return storageStatus.get(name);
	}

	public Map<String, ServerStatus> listServerStatus() {
		return Collections.unmodifiableMap(serverStatus);
	}

	public Map<String, ClientStatus> listClientStatus() {
		return Collections.unmodifiableMap(clientStatus);
	}

	public Map<String, AtomicLong> listServerEventCounters() {
		return Collections.unmodifiableMap(serverParsedEventCount);
	}

	public Map<String, AtomicLong> listServerRowCounters() {
		return Collections.unmodifiableMap(serverParsedRowCount);
	}

	public Map<String, Long> listStorageStatus() {
		return Collections.unmodifiableMap(storageStatus);
	}

	public static class ClientStatus {
		private String		target;
		private boolean		dml;
		private boolean		ddl;
		private boolean		needTsInfo;
		private String		codec;
		private String[]	dt;
		private long		seq;

		public ClientStatus(String target, boolean dml, boolean ddl, boolean needTsInfo, String codec, String[] dt,
				long seq) {
			super();
			this.target = target;
			this.dml = dml;
			this.ddl = ddl;
			this.needTsInfo = needTsInfo;
			this.codec = codec;
			this.dt = dt;
			this.seq = seq;
		}

		/**
		 * @param seq
		 *            the seq to set
		 */
		public void setSeq(long seq) {
			this.seq = seq;
		}

		/**
		 * @return the seq
		 */
		public long getSeq() {
			return seq;
		}

		/**
		 * @return the target
		 */
		public String getTarget() {
			return target;
		}

		/**
		 * @return the dml
		 */
		public boolean isDml() {
			return dml;
		}

		/**
		 * @return the ddl
		 */
		public boolean isDdl() {
			return ddl;
		}

		/**
		 * @return the needTsInfo
		 */
		public boolean isNeedTsInfo() {
			return needTsInfo;
		}

		/**
		 * @return the codec
		 */
		public String getCodec() {
			return codec;
		}

		/**
		 * @return the dt
		 */
		public String[] getDt() {
			return dt;
		}

	}

	public static class ServerStatus {
		private String	binlogFile;
		private long	binlogPos;
		private String	host;
		private int		port;
		private String	db;

		public ServerStatus(String binlogFile, long binlogPos, String host, int port, String db) {
			this.binlogFile = binlogFile;
			this.binlogPos = binlogPos;
			this.host = host;
			this.port = port;
			this.db = db;
		}

		/**
		 * @return the host
		 */
		public String getHost() {
			return host;
		}

		/**
		 * @return the port
		 */
		public int getPort() {
			return port;
		}

		/**
		 * @return the db
		 */
		public String getDb() {
			return db;
		}

		/**
		 * @return the binlogFile
		 */
		public String getBinlogFile() {
			return binlogFile;
		}

		/**
		 * @return the binlogPos
		 */
		public long getBinlogPos() {
			return binlogPos;
		}

	}
}
