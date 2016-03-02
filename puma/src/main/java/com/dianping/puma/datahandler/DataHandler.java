/**
 * Project: ${puma-datahandler.aid}
 * 
 * File Created at 2012-6-25
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
package com.dianping.puma.datahandler;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.event.BinlogEvent;

/**
 * 
 * @author Leo Liang
 * 
 */
public interface DataHandler extends LifeCycle {
	DataHandlerResult process(BinlogEvent binlogEvent, PumaContext context);
}
