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
import com.dianping.puma.common.mysql.BinlogConstanst;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.common.mysql.event.BinlogHeader;
import com.dianping.puma.common.mysql.event.DeleteRowsEvent;
import com.dianping.puma.common.mysql.event.FormatDescriptionEvent;
import com.dianping.puma.common.mysql.event.IncidentEvent;
import com.dianping.puma.common.mysql.event.IntVarEvent;
import com.dianping.puma.common.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.common.mysql.event.QueryEvent;
import com.dianping.puma.common.mysql.event.RandEvent;
import com.dianping.puma.common.mysql.event.RotateEvent;
import com.dianping.puma.common.mysql.event.StopEvent;
import com.dianping.puma.common.mysql.event.TableMapEvent;
import com.dianping.puma.common.mysql.event.UnknownEvent;
import com.dianping.puma.common.mysql.event.UpdateRowsEvent;
import com.dianping.puma.common.mysql.event.UserVarEvent;
import com.dianping.puma.common.mysql.event.WriteRowsEvent;
import com.dianping.puma.common.mysql.event.XIDEvent;
import com.dianping.puma.parser.Parser;

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
		BinlogEvent event = null;
		switch (header.getEventType()) {
			case BinlogConstanst.UNKNOWN_EVENT:
				event = new UnknownEvent();
				break;
			case BinlogConstanst.QUERY_EVENT:
				event = new QueryEvent();
				break;
			case BinlogConstanst.STOP_EVENT:
				event = new StopEvent();
				break;
			case BinlogConstanst.ROTATE_EVENT:
				event = new RotateEvent();
				break;
			case BinlogConstanst.INTVAR_EVENT:
				event = new IntVarEvent();
				break;
			case BinlogConstanst.RAND_EVENT:
				event = new RandEvent();
				break;
			case BinlogConstanst.USER_VAR_EVENT:
				event = new UserVarEvent();
				break;
			case BinlogConstanst.FORMAT_DESCRIPTION_EVENT:
				event = new FormatDescriptionEvent();
				break;
			case BinlogConstanst.XID_EVENT:
				event = new XIDEvent();
				break;
			case BinlogConstanst.TABLE_MAP_EVENT:
				event = new TableMapEvent();
				break;
			case BinlogConstanst.WRITE_ROWS_EVENT:
				event = new WriteRowsEvent();
				break;
			case BinlogConstanst.UPDATE_ROWS_EVENT:
				event = new UpdateRowsEvent();
				break;
			case BinlogConstanst.DELETE_ROWS_EVENT:
				event = new DeleteRowsEvent();
				break;
			case BinlogConstanst.INCIDENT_EVENT:
				event = new IncidentEvent();
				break;
			default:
				event = new PumaIgnoreEvent();
				break;
		}
		if (event != null) {
			event.parse(buf, context, header);
		}

		return event;
	}
}
