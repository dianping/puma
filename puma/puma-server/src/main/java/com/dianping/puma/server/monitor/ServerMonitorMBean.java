/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-30
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
package com.dianping.puma.server.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.puma.common.monitor.BinlogInfoAware;

/**
 * TODO Comment of ServerMonitorMBean
 * 
 * @author Leo Liang
 * 
 */
public class ServerMonitorMBean {
	private String					serverName;
	private List<BinlogInfoAware>	binlogInfos		= new ArrayList<BinlogInfoAware>();
	private Map<String, String>		additionInfos	= new HashMap<String, String>();

	public void addBinlogInfo(BinlogInfoAware binlogInfo) {
		binlogInfos.add(binlogInfo);
	}

	public void addAdditionInfo(String key, String value) {
		additionInfos.put(key, value);
	}

	/**
	 * @param serverName
	 *            the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getBinlogInfoStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append("<b>").append(serverName).append("</b><br>");
		sb.append("<table>");
		sb.append("<tr><td>Server Info</td><td>");
		for (Entry<String, String> entry : additionInfos.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
		}
		sb.append("</td></tr>");
		for (BinlogInfoAware info : binlogInfos) {
			sb.append("<tr>");
			sb.append("<td>").append("name").append("</td><td>").append(info.getMonitorTargetName()).append("</td>");
			sb.append("<td>").append("binlogPos").append("</td><td>").append(info.getBinlogPos()).append("</td>");
			sb.append("<td>").append("binlogFile").append("</td><td>").append(info.getBinlogFile()).append("</td>");

			sb.append("</tr>");
		}
		sb.append("</table>");

		return sb.toString();
	}
}
