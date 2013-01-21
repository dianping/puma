/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-11 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.bo;

import java.io.Serializable;
import java.util.Map;

import com.dianping.puma.core.LRUCache;
import com.dianping.puma.parser.mysql.event.TableMapEvent;

/**
 * @author Leo Liang
 * 
 */
public class PumaContext implements Serializable {
	private static final long			serialVersionUID		= -2280369356150286536L;
	private String						serverVersion			= null;
	private int							serverMajorVersion		= 0;
	private int							serverMinorVersion		= 0;
	private int							serverSubMinorVersion	= 0;
	private int							maxThreeBytes			= 255 * 255 * 255;
	private byte						protocolVersion			= 0;
	private long						threadId;
	private String						seed;
	private int							serverCapabilities;
	private int							serverCharsetIndex;
	private int							serverStatus			= 0;
	private int							clientParam				= 0;
	private boolean						has41NewNewProt			= false;
	private boolean						use41Extensions			= false;
	private String						encoding				= "utf-8";
	private String						binlogFileName;
	private long						binlogStartPos;
	private long						pumaServerId;
	private String						pumaServerName;
	private Map<Long, TableMapEvent>	tableMaps				= new LRUCache<Long, TableMapEvent>(300);
	private String						masterUrl;
	private long						nextBinlogPos;

	/**
	 * @return the nextBinlogPos
	 */
	public long getNextBinlogPos() {
		return nextBinlogPos;
	}

	/**
	 * @param nextBinlogPos
	 *            the nextBinlogPos to set
	 */
	public void setNextBinlogPos(long nextBinlogPos) {
		this.nextBinlogPos = nextBinlogPos;
	}

	/**
	 * @return the masterUrl
	 */
	public String getMasterUrl() {
		return masterUrl;
	}

	/**
	 * @param masterUrl
	 *            the masterUrl to set
	 */
	public void setMasterUrl(String host, int port) {
		this.masterUrl = host + ":" + port;
	}

	/**
	 * @return the pumaServerName
	 */
	public String getPumaServerName() {
		return pumaServerName;
	}

	/**
	 * @param pumaServerName
	 *            the pumaServerName to set
	 */
	public void setPumaServerName(String pumaServerName) {
		this.pumaServerName = pumaServerName;
	}

	/**
	 * @return the pumaServerId
	 */
	public long getPumaServerId() {
		return pumaServerId;
	}

	/**
	 * @param pumaServerId
	 *            the pumaServerId to set
	 */
	public void setPumaServerId(long pumaServerId) {
		this.pumaServerId = pumaServerId;
	}

	/**
	 * @return the tableMaps
	 */
	public Map<Long, TableMapEvent> getTableMaps() {
		return tableMaps;
	}

	/**
	 * @param tableMaps
	 *            the tableMaps to set
	 */
	public void setTableMaps(Map<Long, TableMapEvent> tableMaps) {
		this.tableMaps = tableMaps;
	}

	public String getBinlogFileName() {
		return binlogFileName;
	}

	public void setBinlogFileName(String binlogFileName) {
		this.binlogFileName = binlogFileName;
	}

	public long getBinlogStartPos() {
		return binlogStartPos;
	}

	public void setBinlogStartPos(long binlogStartPos) {
		this.binlogStartPos = binlogStartPos;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isUse41Extensions() {
		return use41Extensions;
	}

	public void setUse41Extensions(boolean use41Extensions) {
		this.use41Extensions = use41Extensions;
	}

	public boolean isHas41NewNewProt() {
		return has41NewNewProt;
	}

	public void setHas41NewNewProt(boolean has41NewNewProt) {
		this.has41NewNewProt = has41NewNewProt;
	}

	public int getClientParam() {
		return clientParam;
	}

	public void setClientParam(int clientParam) {
		this.clientParam = clientParam;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	public int getServerMajorVersion() {
		return serverMajorVersion;
	}

	public void setServerMajorVersion(int serverMajorVersion) {
		this.serverMajorVersion = serverMajorVersion;
	}

	public int getServerMinorVersion() {
		return serverMinorVersion;
	}

	public void setServerMinorVersion(int serverMinorVersion) {
		this.serverMinorVersion = serverMinorVersion;
	}

	public int getServerSubMinorVersion() {
		return serverSubMinorVersion;
	}

	public void setServerSubMinorVersion(int serverSubMinorVersion) {
		this.serverSubMinorVersion = serverSubMinorVersion;
	}

	public int getMaxThreeBytes() {
		return maxThreeBytes;
	}

	public void setMaxThreeBytes(int maxThreeBytes) {
		this.maxThreeBytes = maxThreeBytes;
	}

	public byte getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(byte protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public int getServerCapabilities() {
		return serverCapabilities;
	}

	public void setServerCapabilities(int serverCapabilities) {
		this.serverCapabilities = serverCapabilities;
	}

	public int getServerCharsetIndex() {
		return serverCharsetIndex;
	}

	public void setServerCharsetIndex(int serverCharsetIndex) {
		this.serverCharsetIndex = serverCharsetIndex;
	}

	public int getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(int serverStatus) {
		this.serverStatus = serverStatus;
	}

}
