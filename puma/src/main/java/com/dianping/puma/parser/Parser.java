/**
 * Project: ${puma-parser.aid}
 * 
 * File Created at 2012-6-23
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
package com.dianping.puma.parser;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.parser.mysql.event.BinlogEvent;

/**
 * TODO Comment of Parser
 * 
 * @author Leo Liang
 * 
 */
public interface Parser extends LifeCycle<Exception> {
	BinlogEvent parse(ByteBuffer buf, PumaContext context) throws IOException;
}
