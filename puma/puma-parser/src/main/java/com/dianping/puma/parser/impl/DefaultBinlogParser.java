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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

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
	private static final Logger								log			= Logger.getLogger(DefaultBinlogParser.class);
	private static Map<Byte, Class<? extends BinlogEvent>>	eventMaps	= new ConcurrentHashMap<Byte, Class<? extends BinlogEvent>>();

	@Override
	public BinlogEvent parse(ByteBuffer buf, PumaContext context) throws IOException {
		BinlogHeader header = new BinlogHeader();
		header.parse(buf, context);
		BinlogEvent event = null;
		Class<? extends BinlogEvent> eventClass = eventMaps.get(header.getEventType());
		if (eventClass != null) {
			try {
				event = eventClass.newInstance();
			} catch (Exception e) {
				log.error("Init event class failed. eventType: " + header.getEventType(), e);
				event = null;
			}
		}

		if (event == null) {
			event = new PumaIgnoreEvent();
		}
		event.parse(buf, context, header);

		return event;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {
		eventMaps.put(BinlogConstanst.UNKNOWN_EVENT, UnknownEvent.class);
		eventMaps.put(BinlogConstanst.QUERY_EVENT, QueryEvent.class);
		eventMaps.put(BinlogConstanst.STOP_EVENT, StopEvent.class);
		eventMaps.put(BinlogConstanst.ROTATE_EVENT, RotateEvent.class);
		eventMaps.put(BinlogConstanst.INTVAR_EVENT, IntVarEvent.class);
		eventMaps.put(BinlogConstanst.RAND_EVENT, RandEvent.class);
		eventMaps.put(BinlogConstanst.USER_VAR_EVENT, UserVarEvent.class);
		eventMaps.put(BinlogConstanst.FORMAT_DESCRIPTION_EVENT, FormatDescriptionEvent.class);
		eventMaps.put(BinlogConstanst.XID_EVENT, XIDEvent.class);
		eventMaps.put(BinlogConstanst.TABLE_MAP_EVENT, TableMapEvent.class);
		eventMaps.put(BinlogConstanst.WRITE_ROWS_EVENT, WriteRowsEvent.class);
		eventMaps.put(BinlogConstanst.UPDATE_ROWS_EVENT, UpdateRowsEvent.class);
		eventMaps.put(BinlogConstanst.DELETE_ROWS_EVENT, DeleteRowsEvent.class);
		eventMaps.put(BinlogConstanst.INCIDENT_EVENT, IncidentEvent.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}
}
