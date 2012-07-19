/**
 * Project: puma-client
 * 
 * File Created at 2012-7-19
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
package com.dianping.puma.api;

import com.dianping.puma.core.event.ChangedEvent;

/**
 * @author Leo Liang
 * 
 */
public interface SkipEventHandler {

	public void notifySkipEvent(ChangedEvent event);

}
