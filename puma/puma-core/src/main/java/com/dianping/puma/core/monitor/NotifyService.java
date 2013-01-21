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

import java.util.Map;

/**
 * @author Leo Liang
 * 
 */
public interface NotifyService {
	public void alarm(String msg, Throwable t, boolean sendSms);

	public void report(String title, Map<String, Map<String, String>> msg);
}
