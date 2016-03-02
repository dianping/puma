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
package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * TODO Comment of BinlogEvent
 * 
 * @author Leo Liang
 * 
 */
public interface BinlogEvent extends Serializable {
	BinlogHeader getHeader();
	
	void setHeader(BinlogHeader header);

	void parse(ByteBuffer buf, PumaContext context, BinlogHeader header) throws IOException;
}
