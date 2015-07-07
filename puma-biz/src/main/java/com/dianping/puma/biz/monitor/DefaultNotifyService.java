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
package com.dianping.puma.biz.monitor;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Leo Liang
 * 
 */
public class DefaultNotifyService implements NotifyService {

	private static final Logger log = Logger.getLogger(DefaultNotifyService.class);

	private static final String MAIL_ALARM_TITLE = "[Puma] Alarm Notify";

	private static final String MAIL_RECOVERY_TITLE = "[Puma] Recovery Notify";

	@Override
	public void alarm(String msg, Throwable t, boolean sendSms) {
		log.error(MAIL_ALARM_TITLE + " : " + msg, t);
	}

	@Override
	public void recover(String msg, boolean sendSms) {
		log.info(MAIL_RECOVERY_TITLE + " : " + msg);
	}

	@Override
	public void report(String title, Map<String, Map<String, String>> msg) {
		log.info("[Puma Report]" + msg);
	}
}
