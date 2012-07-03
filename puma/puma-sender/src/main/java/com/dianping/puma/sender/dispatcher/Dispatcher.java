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
package com.dianping.puma.sender.dispatcher;

import java.util.List;

import com.dianping.puma.client.ChangedEvent;
import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.sender.Sender;

/**
 * TODO Comment of Dispatcher
 * 
 * @author Leo Liang
 * 
 */
public interface Dispatcher extends LifeCycle {
	public String getName();

	public void dispatch(ChangedEvent event, PumaContext context) throws Exception;

	public List<Sender> getSenders();
}
