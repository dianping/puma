/**
 * Project: ${puma-sender.aid}
 * 
 * File Created at 2012-6-27
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
package com.dianping.puma.sender;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.common.bo.PumaContext;

/**
 * TODO Comment of Sender
 * 
 * @author Leo Liang
 * 
 */
public interface Sender extends LifeCycle {
	public String getName();

	public void send(DataChangedEvent event, PumaContext context) throws Exception;
}