/**
 * Project: ${puma-parser.aid}
 * 
 * File Created at 2012-6-24
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
package com.dianping.puma.parser.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.parser.mysql.BinlogConstanst;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.BinlogHeader;
import com.dianping.puma.parser.mysql.event.QueryEvent;

/**
 * TODO Comment of DefaultBinlogParser
 * 
 * @author Leo Liang
 * 
 */
public class DefaultBinlogParser implements Parser {

	@Override
	public BinlogEvent parse(ByteBuffer buf, PumaContext context) throws IOException {
		BinlogHeader header = new BinlogHeader();
		header.parse(buf, context);
		switch (header.getEventType()) {
			case BinlogConstanst.QUERY_EVENT:
				BinlogEvent event = new QueryEvent();
				event.parse(buf, context, header);
				return event;

			default:
				return null;
		}
	}

}
