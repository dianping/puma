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

	private static final Logger	log						= Logger.getLogger(DailyReportTask.class);
	private NotifyService		notifyService;
	private Map<String, Long>	lastDayParsedUpdateRows	= new HashMap<String, Long>();
	private Map<String, Long>	lastDayParsedInsertRows	= new HashMap<String, Long>();
	private Map<String, Long>	lastDayParsedDeleteRows	= new HashMap<String, Long>();
	private Map<String, Long>	lastDayParsedDdlEvents	= new HashMap<String, Long>();
	private String				lastDay					= "2012-07-22";

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

								Long lastDayParsedRowsInsertCounter = lastDayParsedInsertRows
										.get(serverStatus.getKey());
								if (lastDayParsedRowsInsertCounter == null) {
									lastDayParsedRowsInsertCounter = SystemStatusContainer.instance
											.listServerRowInsertCounters().get(serverStatus.getKey()).longValue();
								} else {
									lastDayParsedRowsInsertCounter = SystemStatusContainer.instance
											.listServerRowInsertCounters().get(serverStatus.getKey()).longValue()
											- lastDayParsedRowsInsertCounter;
								}

								lastDayParsedInsertRows.put(serverStatus.getKey(), lastDayParsedRowsInsertCounter);

								Long lastDayParsedRowsUpdateCounter = lastDayParsedUpdateRows
										.get(serverStatus.getKey());
								if (lastDayParsedRowsUpdateCounter == null) {
									lastDayParsedRowsUpdateCounter = SystemStatusContainer.instance
											.listServerRowUpdateCounters().get(serverStatus.getKey()).longValue();
								} else {
									lastDayParsedRowsUpdateCounter = SystemStatusContainer.instance
											.listServerRowUpdateCounters().get(serverStatus.getKey()).longValue()
											- lastDayParsedRowsUpdateCounter;
								}

								lastDayParsedUpdateRows.put(serverStatus.getKey(), lastDayParsedRowsUpdateCounter);

								Long lastDayParsedRowsDeleteCounter = lastDayParsedDeleteRows
										.get(serverStatus.getKey());
								if (lastDayParsedRowsDeleteCounter == null) {
									lastDayParsedRowsDeleteCounter = SystemStatusContainer.instance
											.listServerRowDeleteCounters().get(serverStatus.getKey()).longValue();
								} else {
									lastDayParsedRowsDeleteCounter = SystemStatusContainer.instance
											.listServerRowDeleteCounters().get(serverStatus.getKey()).longValue()
											- lastDayParsedRowsDeleteCounter;
								}

								lastDayParsedDeleteRows.put(serverStatus.getKey(), lastDayParsedRowsDeleteCounter);

								Long lastDayDdlEventsCounter = lastDayParsedDdlEvents.get(serverStatus.getKey());
								if (lastDayDdlEventsCounter == null) {
									lastDayDdlEventsCounter = SystemStatusContainer.instance.listServerDdlCounters()
											.get(serverStatus.getKey()).longValue();
								} else {
									lastDayDdlEventsCounter = SystemStatusContainer.instance.listServerDdlCounters()
											.get(serverStatus.getKey()).longValue()
											- lastDayDdlEventsCounter;
								}

								lastDayParsedDdlEvents.put(serverStatus.getKey(), lastDayDdlEventsCounter);

								serverStatusMap.put("parsed rows insert(since last day)",
										Long.toString(lastDayParsedRowsInsertCounter));
								serverStatusMap.put("parsed rows update(since last day)",
										Long.toString(lastDayParsedRowsUpdateCounter));
								serverStatusMap.put("parsed rows delete(since last day)",
										Long.toString(lastDayParsedRowsDeleteCounter));
								serverStatusMap.put("parsed ddl events(since last day)",
										Long.toString(lastDayDdlEventsCounter));
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