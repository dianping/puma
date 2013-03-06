/**
 * Project: puma-server
 * 
 * File Created at 2012-7-22
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
package com.dianping.puma.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ServerStatus;
import com.dianping.puma.core.monitor.Notifiable;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.storage.Sequence;

/**
 * 
 * @author Leo Liang
 * 
 */
public class StatusReportTask implements Task, Notifiable {
	private static final Logger	log	= Logger.getLogger(StatusReportTask.class);
	private NotifyService		notifyService;

	/**
	 * @param notifyService
	 *            the notifyService to set
	 */
	public void setNotifyService(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

	public void start() {
		PumaThreadUtils.createThread(new Runnable() {

			@Override
			public void run() {
				boolean first = true;
				while (true) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					try {

						if (notifyService != null && !first) {
							log.info("Status report start...");

							notifyService.report("[Puma] Status Report", getStatus());
						}
						TimeUnit.MINUTES.sleep(getStatusReportInterval());
						first = false;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} catch (Exception ex) {
						log.error("Status report failed.", ex);
					}
				}
			}

		}, "StatusReport", true).start();
	}

	protected Map<String, Map<String, String>> getStatus() {
		Map<String, Map<String, String>> statuses = new HashMap<String, Map<String, String>>();
		Map<String, String> serverStatusMap = new HashMap<String, String>();
		statuses.put("Server Status", serverStatusMap);

		Map<String, String> storageStatusMap = new HashMap<String, String>();
		statuses.put("Storage Status", storageStatusMap);

		Map<String, ServerStatus> serverStatuses = SystemStatusContainer.instance.listServerStatus();
		Map<String, Long> storageStatuses = SystemStatusContainer.instance.listStorageStatus();

		for (Map.Entry<String, ServerStatus> serverStatus : serverStatuses.entrySet()) {
			serverStatusMap.put("name", serverStatus.getKey());
			serverStatusMap.put("host", serverStatus.getValue().getHost());
			serverStatusMap.put("port", Integer.toString(serverStatus.getValue().getPort()));
			serverStatusMap.put("binLogFile", serverStatus.getValue().getBinlogFile());
			serverStatusMap.put("binLogPos", Long.toString(serverStatus.getValue().getBinlogPos()));
			AtomicLong updatedRows = SystemStatusContainer.instance.listServerRowUpdateCounters().get(
					serverStatus.getKey());
			serverStatusMap.put("parsed rows update(since start)",
					Long.toString(updatedRows == null ? 0 : updatedRows.longValue()));
			AtomicLong deletedRows = SystemStatusContainer.instance.listServerRowDeleteCounters().get(
					serverStatus.getKey());
			serverStatusMap.put("parsed rows delete(since start)",
					Long.toString(deletedRows == null ? 0 : deletedRows.longValue()));
			AtomicLong insertedRows = SystemStatusContainer.instance.listServerRowInsertCounters().get(
					serverStatus.getKey());
			serverStatusMap.put("parsed rows insert(since start)",
					Long.toString(insertedRows == null ? 0 : insertedRows.longValue()));
			AtomicLong ddls = SystemStatusContainer.instance.listServerDdlCounters().get(serverStatus.getKey());
			serverStatusMap.put("parsed ddl events(since start)", Long.toString(ddls == null ? 0 : ddls.longValue()));
		}

		for (Map.Entry<String, Long> storageStatus : storageStatuses.entrySet()) {
			storageStatusMap.put("name", storageStatus.getKey());
			storageStatusMap.put("seq",
					storageStatus.getValue() + "&nbsp;&nbsp;" + new Sequence(storageStatus.getValue()).toString());
		}
		return statuses;
	}

	private int getStatusReportInterval() {
		try {
			return ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getIntProperty(
					"puma.statusreport.interval");
		} catch (LionException e) {
			return 60;
		}
	}
}
