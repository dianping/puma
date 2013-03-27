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
package com.dianping.puma.core.monitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.hawk.common.alarm.service.CommonAlarmService;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.util.IPUtils;

/**
 * @author Leo Liang
 * 
 */
public class DefaultNotifyService implements NotifyService {

	private static final Logger	log					= Logger.getLogger(DefaultNotifyService.class);
	private static final String	MAIL_ALARM_TITLE	= "[Puma] Alarm Notify";
	private static final String MAIL_RECOVERY_TITLE = "[Puma] Recovery Notify";
	private static final String	KEY_MAIL_TO			= "puma.notify.mailTo";
	private static final String	KEY_SMS_TO			= "puma.notify.smsTo";
	private CommonAlarmService	alarmService;
	private String				localIP				= IPUtils.getFirstNoLoopbackIP4Address();
	private boolean				devMode				= false;

	/**
	 * @param devMode
	 *            the devMode to set
	 */
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}

	/**
	 * @param alarmService
	 *            the alarmService to set
	 */
	public void setAlarmService(CommonAlarmService alarmService) {
		this.alarmService = alarmService;
	}

	@Override
	public void alarm(String msg, Throwable t, boolean sendSms) {
		log.error(MAIL_ALARM_TITLE + " : " + msg, t);

		if (!devMode) {
			StringBuilder body = new StringBuilder();
			body.append("<strong>").append(msg).append("</strong><br/>");
			body.append("<br/>");
			if (t != null) {
				body.append("<b>").append("Exception message:").append("&nbsp;</b>").append(t.getClass().getName())
						.append(":").append(t.getMessage()).append("<br/>");
				body.append("<i>").append("Stack trace:").append("&nbsp;<i><br/>");
				body.append(displayErrorForHtml(t));
			}
			try {
				this.alarmService.sendEmail(body.toString(), MAIL_ALARM_TITLE + "_" + localIP, getMailTos());
				if (sendSms) {
					List<String> numbers = getPhoneNums();
					if (numbers != null && numbers.size() > 0) {
						this.alarmService.sendSmsMessage("[Puma Alarm]" + "_" + localIP + ":" + StringUtils.abbreviate(msg, 1000), numbers);
					}
				}
			} catch (Exception e) {
				log.warn("Alarm failed. BODY: " + body);
			}
		}
	}

	@Override
    public void recover(String msg, boolean sendSms) {
        log.info(MAIL_RECOVERY_TITLE + " : " + msg);

        if (!devMode) {
            StringBuilder body = new StringBuilder();
            body.append("<strong>").append(msg).append("</strong><br/>");
            body.append("<br/>");
            try {
                this.alarmService.sendEmail(body.toString(), MAIL_RECOVERY_TITLE + "_" + localIP, getMailTos());
                if (sendSms) {
                    List<String> numbers = getPhoneNums();
                    if (numbers != null && numbers.size() > 0) {
                        this.alarmService.sendSmsMessage("[Puma Recovery]" + "_" + localIP + ":" + StringUtils.abbreviate(msg, 1000), numbers);
                    }
                }
            } catch (Exception e) {
                log.warn("Alarm failed. BODY: " + body);
            }
        }
    }

	@Override
	public void report(String title, Map<String, Map<String, String>> msg) {
		log.info("[Puma Report]" + msg);
		if (!devMode) {
			StringBuilder body = new StringBuilder();
			body.append("<strong>Puma Status Report</strong><br/>");
			for (Map.Entry<String, Map<String, String>> entry : msg.entrySet()) {
				body.append("<table align=\"left\" frame= \"box\" width=\"90%\"><tbody align=\"center\">");
				body.append("<tr align=\"center\" colspan=\"100%\"><td><font color=\"00005f\" size=\"3\"><b>").append(entry.getKey()).append("</b></font></td></tr>");
				body.append("<tr bgcolor=\"ffd588\"><th>name</th><th>value</th></tr>");
				int row = 1;
				for (Map.Entry<String, String> suEntry : entry.getValue().entrySet()) {
					if(row++ % 2 == 0){
						body.append("<tr bgcolor=\"e2e2e2\"><td>").append(suEntry.getKey()).append("</td><td>").append(suEntry.getValue())
						.append("</td></tr>");
					}else{
					body.append("<tr><td>").append(suEntry.getKey()).append("</td><td>").append(suEntry.getValue())
							.append("</td></tr>");
					}
				}
				body.append("</table><br /><br />");
			}
			try {
				this.alarmService.sendEmail(body.toString(), title + "_" + localIP, getMailTos());
			} catch (Exception e) {
				log.warn("Report failed. BODY: " + body);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> getPhoneNums() throws LionException {
		String smsToStr = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty(KEY_SMS_TO);
		if (smsToStr != null && smsToStr.trim().length() > 0) {
			return Arrays.asList(smsToStr.split(","));
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	protected String displayErrorForHtml(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace.replace(System.getProperty("line.separator"), "<br/>\n");
	}

	protected List<String> getMailTos() throws LionException {
		String mailToStr = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty(KEY_MAIL_TO);
		return Arrays.asList(mailToStr.split(","));
	}

}
