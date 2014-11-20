/**
 * Project: puma-client
 * 
 * File Created at 2012-7-5
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
package com.dianping.puma.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.puma.core.constant.SubscribeConstant;

/**
 * TODO Comment of Configuration
 * 
 * @author Leo Liang
 * 
 */
public class Configuration implements Serializable {
	private static final long serialVersionUID = 5160892707659109303L;
	private String codecType = "json";
	private Map<String, List<String>> databaseTablesMapping = new HashMap<String, List<String>>();
	private String host;
	private boolean needDdl = false;
	private boolean needDml = true;
	private boolean needTransactionInfo = false;
	private int port = 7862;
	private String name;
	private String seqFileBase = "/data/applogs/puma/";
	private String target;
	private long serverId = -1L;
	private String binlog;
	private long binlogPos = -1;
	private long timeStamp = -1L;

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the binlog
	 */
	public String getBinlog() {
		return binlog;
	}

	/**
	 * @return the binlogPos
	 */
	public long getBinlogPos() {
		return binlogPos;
	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @return the serverId
	 */
	public long getServerId() {
		return serverId;
	}

	/**
	 * @param serverId
	 *            the serverId to set
	 */
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	/**
	 * @param binlog
	 *            the binlog to set
	 */
	public void setBinlog(String binlog) {
		this.binlog = binlog;
	}

	/**
	 * @param binlogPos
	 *            the binlogPos to set
	 */
	public void setBinlogPos(long binlogPos) {
		this.binlogPos = binlogPos;
	}

	public void addDatabaseTable(String database, String... tablePatterns) {
		if (!this.databaseTablesMapping.containsKey(database)) {
			this.databaseTablesMapping.put(database, new ArrayList<String>(Arrays.asList(tablePatterns)));
		} else {
			this.databaseTablesMapping.get(database).addAll(Arrays.asList(tablePatterns));
		}
	}

	public String buildRequestParamString(long seq) {
		StringBuilder param = new StringBuilder();
		param.append("seq=").append(seq);
		if (binlogPos != -1L && binlog != null && serverId != -1L && seq == SubscribeConstant.SEQ_FROM_BINLOGINFO) {
			param.append("&binlog=").append(binlog);
			param.append("&binlogPos=").append(binlogPos);
			param.append("&serverId=").append(serverId);
		} else if (timeStamp != -1L && seq == SubscribeConstant.SEQ_FROM_TIMESTAMP) {
			param.append("&timestamp=").append(timeStamp);
		}
		param.append("&name=").append(name);
		param.append("&target=").append(target);
		param.append("&ddl=").append(needDdl);
		param.append("&dml=").append(needDml);
		param.append("&ts=").append(needTransactionInfo);
		param.append("&codec=").append(codecType);
		for (Map.Entry<String, List<String>> entry : databaseTablesMapping.entrySet()) {
			for (String tb : entry.getValue()) {
				param.append("&dt=").append(entry.getKey()).append(".").append(tb);
			}
		}

		return param.toString();
	}

	/**
	 * @return the seqFileBase
	 */
	public String getSeqFileBase() {
		return seqFileBase;
	}

	/**
	 * @param seqFileBase
	 *            the seqFileBase to set
	 */
	public void setSeqFileBase(String seqFileBase) {
		this.seqFileBase = seqFileBase;
	}

	/**
	 * @return
	 */
	public String buildUrl() {
		return "http://" + host + ":" + port + "/puma/channel";
	}

	/**
	 * @return the codecType
	 */
	public String getCodecType() {
		return codecType;
	}

	/**
	 * @return the databaseTablesMapping
	 */
	public Map<String, List<String>> getDatabaseTablesMapping() {
		return databaseTablesMapping;
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
	 * @return the needDdl
	 */
	public boolean isNeedDdl() {
		return needDdl;
	}

	/**
	 * @return the needDml
	 */
	public boolean isNeedDml() {
		return needDml;
	}

	/**
	 * @return the needTransactionInfo
	 */
	public boolean isNeedTransactionInfo() {
		return needTransactionInfo;
	}

	/**
	 * @param codecType
	 */
	public void setCodecType(String codecType) {
		this.codecType = codecType;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param needDdl
	 *            the needDdl to set
	 */
	public void setNeedDdl(boolean needDdl) {
		this.needDdl = needDdl;
	}

	/**
	 * @param needDml
	 *            the needDml to set
	 */
	public void setNeedDml(boolean needDml) {
		this.needDml = needDml;
	}

	/**
	 * @param needTransactionInfo
	 *            the needTransactionInfo to set
	 */
	public void setNeedTransactionInfo(boolean needTransactionInfo) {
		this.needTransactionInfo = needTransactionInfo;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Configuration [codecType=" + codecType + ", databaseTablesMapping=" + databaseTablesMapping + ", host="
				+ host + ", needDdl=" + needDdl + ", needDml=" + needDml + ", needTransactionInfo="
				+ needTransactionInfo + ", port=" + port + ", name=" + name + ", seqFileBase=" + seqFileBase
				+ ", target=" + target + ", serverId=" + serverId + ", binlog=" + binlog + ", binlogPos=" + binlogPos
				+ "]";
	}

	public void validate() {
		if (host == null || host.trim().length() == 0) {
			throw new IllegalArgumentException("Puma client's host not set.");
		}
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Puma client's name not set.");
		}
		if (target == null || target.trim().length() == 0) {
			throw new IllegalArgumentException("Puma client's target not set.");
		}
		if (databaseTablesMapping == null || databaseTablesMapping.size() == 0) {
			throw new IllegalArgumentException("Puma client's db&tb not set.");
		}
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
