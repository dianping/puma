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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO Comment of Configuration
 * 
 * @author Leo Liang
 * 
 */
public class Configuration implements Serializable {
	private static final long serialVersionUID = 5160892707659109303L;

	private String host;

	private int port = 7862;

	private Map<String, List<String>> databaseTablesMapping = new HashMap<String, List<String>>();

	private boolean needDdl = false;

	private boolean needDml = true;

	private boolean needTransactionInfo = false;

	/**
	 * @return the needDml
	 */
	public boolean isNeedDml() {
		return needDml;
	}

	/**
	 * @param needDml
	 *           the needDml to set
	 */
	public void setNeedDml(boolean needDml) {
		this.needDml = needDml;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *           the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *           the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the databaseTablesMapping
	 */
	public Map<String, List<String>> getDatabaseTablesMapping() {
		return databaseTablesMapping;
	}

	public void addDatabaseTable(String database, String... tablePatterns) {
		if (!this.databaseTablesMapping.containsKey(database)) {
			this.databaseTablesMapping.put(database, Arrays.asList(tablePatterns));
		} else {
			this.databaseTablesMapping.get(database).addAll(Arrays.asList(tablePatterns));
		}
	}

	/**
	 * @return the needDdl
	 */
	public boolean isNeedDdl() {
		return needDdl;
	}

	/**
	 * @param needDdl
	 *           the needDdl to set
	 */
	public void setNeedDdl(boolean needDdl) {
		this.needDdl = needDdl;
	}

	/**
	 * @return the needTransactionInfo
	 */
	public boolean isNeedTransactionInfo() {
		return needTransactionInfo;
	}

	/**
	 * @param needTransactionInfo
	 *           the needTransactionInfo to set
	 */
	public void setNeedTransactionInfo(boolean needTransactionInfo) {
		this.needTransactionInfo = needTransactionInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Configuration [host=" + host + ", port=" + port + ", databaseTablesMapping=" + databaseTablesMapping
		      + ", needDdl=" + needDdl + ", needDml=" + needDml + ", needTransactionInfo=" + needTransactionInfo + "]";
	}

	public void validate() {
		if (host == null || host.trim().length() == 0) {
			throw new IllegalArgumentException("Puma client's host not set.");
		}
	}
}
