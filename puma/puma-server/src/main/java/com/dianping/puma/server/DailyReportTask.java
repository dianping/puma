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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.puma.common.Notifiable;
import com.dianping.puma.common.NotifyService;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ServerStatus;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.storage.Sequence;

/**
 * @author Leo Liang
 * 
 */
public class DailyReportTask implements Task, Notifiable {

	private static final Logger	log					= Logger.getLogger(DailyReportTask.class);
	private NotifyService		notifyService;
	private Map<String, Long>	lastDayParsedRows	= new HashMap<String, Long>();
	private Map<String, Long>	lastDayParsedEvents	= new HashMap<String, Long>();
	private String				lastDay				= "2012-07-22";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Task#start()
	 */
	@Override
	public void start() {

		PumaThreadUtils.createThread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					try {

						if (notifyService != null && needReport()) {
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

								Long lastDayParsedEventCounter = lastDayParsedEvents.get(serverStatus.getKey());
								if (lastDayParsedEventCounter == null) {
									lastDayParsedEventCounter = SystemStatusContainer.instance
											.listServerEventCounters().get(serverStatus.getKey()).longValue();
								} else {
									lastDayParsedEventCounter = SystemStatusContainer.instance
											.listServerEventCounters().get(serverStatus.getKey()).longValue()
											- lastDayParsedEventCounter;
								}

								lastDayParsedEvents.put(serverStatus.getKey(), lastDayParsedEventCounter);

								Long lastDayParsedRowCounter = lastDayParsedRows.get(serverStatus.getKey());
								if (lastDayParsedRowCounter == null) {
									lastDayParsedRowCounter = SystemStatusContainer.instance.listServerRowCounters()
											.get(serverStatus.getKey()).longValue();
								} else {
									lastDayParsedRowCounter = SystemStatusContainer.instance.listServerRowCounters()
											.get(serverStatus.getKey()).longValue()
											- lastDayParsedRowCounter;
								}

								serverStatusMap.put("parsed events(since last day)",
										Long.toString(lastDayParsedEventCounter));
								serverStatusMap.put("parsed rows(since last day)",
										Long.toString(lastDayParsedRowCounter));
							}

							for (Map.Entry<String, Long> storageStatus : storageStatuses.entrySet()) {
								storageStatusMap.put("name", storageStatus.getKey());
								storageStatusMap.put("seq",
										storageStatus.getValue() + new Sequence(storageStatus.getValue()).toString());
							}

							notifyService.report("[Puma] Daily Report(" + lastDay + ")", statuses);
						}
						TimeUnit.MINUTES.sleep(20);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} catch (Exception ex) {
						log.error("Daily task failed.", ex);
					}
				}
			}
		}, "DailyReport", true).start();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.common.Notifiable#setNotifyService(com.dianping.puma
	 * .common.NotifyService)
	 */
	@Override
	public void setNotifyService(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

	private boolean needReport() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (cal.get(Calendar.HOUR_OF_DAY) == 1) {
			lastDay = sdf.format(cal.getTime());
			return true;
		} else {
			return false;
		}
	}
}
