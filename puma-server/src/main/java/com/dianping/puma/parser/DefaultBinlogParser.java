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
package com.dianping.puma.parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.event.AnonymousGtidEvent;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.BinlogHeader;
import com.dianping.puma.parser.mysql.event.DeleteRowsEvent;
import com.dianping.puma.parser.mysql.event.FormatDescriptionEvent;
import com.dianping.puma.parser.mysql.event.GtidEvent;
import com.dianping.puma.parser.mysql.event.HeartbeatEvent;
import com.dianping.puma.parser.mysql.event.IgnorableEvent;
import com.dianping.puma.parser.mysql.event.IncidentEvent;
import com.dianping.puma.parser.mysql.event.IntVarEvent;
import com.dianping.puma.parser.mysql.event.PreviousGtidsEvent;
import com.dianping.puma.parser.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.parser.mysql.event.QueryEvent;
import com.dianping.puma.parser.mysql.event.RandEvent;
import com.dianping.puma.parser.mysql.event.RotateEvent;
import com.dianping.puma.parser.mysql.event.RowsQueryEvent;
import com.dianping.puma.parser.mysql.event.StopEvent;
import com.dianping.puma.parser.mysql.event.TableMapEvent;
import com.dianping.puma.parser.mysql.event.UnknownEvent;
import com.dianping.puma.parser.mysql.event.UpdateRowsEvent;
import com.dianping.puma.parser.mysql.event.UserVarEvent;
import com.dianping.puma.parser.mysql.event.WriteRowsEvent;
import com.dianping.puma.parser.mysql.event.XIDEvent;

/**
 * TODO Comment of DefaultBinlogParser
 * 
 * @author Leo Liang
 * 
 */
@ThreadSafe
public class DefaultBinlogParser implements Parser {
	private static final Logger logger = Logger.getLogger(DefaultBinlogParser.class);
	private static Map<Byte, Class<? extends BinlogEvent>> eventMaps = new ConcurrentHashMap<Byte, Class<? extends BinlogEvent>>();

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
				logger.error("Init event class failed. eventType: " + header.getEventType(), e);
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
		eventMaps.put(BinlogConstants.UNKNOWN_EVENT, UnknownEvent.class);
		eventMaps.put(BinlogConstants.QUERY_EVENT, QueryEvent.class);
		eventMaps.put(BinlogConstants.STOP_EVENT, StopEvent.class);
		eventMaps.put(BinlogConstants.ROTATE_EVENT, RotateEvent.class);
		eventMaps.put(BinlogConstants.INTVAR_EVENT, IntVarEvent.class);
		eventMaps.put(BinlogConstants.RAND_EVENT, RandEvent.class);
		eventMaps.put(BinlogConstants.USER_VAR_EVENT, UserVarEvent.class);
		eventMaps.put(BinlogConstants.FORMAT_DESCRIPTION_EVENT, FormatDescriptionEvent.class);
		eventMaps.put(BinlogConstants.XID_EVENT, XIDEvent.class);
		eventMaps.put(BinlogConstants.TABLE_MAP_EVENT, TableMapEvent.class);
		eventMaps.put(BinlogConstants.WRITE_ROWS_EVENT_V1, WriteRowsEvent.class);
		eventMaps.put(BinlogConstants.UPDATE_ROWS_EVENT_V1, UpdateRowsEvent.class);
		eventMaps.put(BinlogConstants.DELETE_ROWS_EVENT_V1, DeleteRowsEvent.class);
		eventMaps.put(BinlogConstants.INCIDENT_EVENT, IncidentEvent.class);
		//mysql --5.6
		eventMaps.put(BinlogConstants.WRITE_ROWS_EVENT, WriteRowsEvent.class);
		eventMaps.put(BinlogConstants.UPDATE_ROWS_EVENT, UpdateRowsEvent.class);
		eventMaps.put(BinlogConstants.DELETE_ROWS_EVENT, DeleteRowsEvent.class);
		eventMaps.put(BinlogConstants.HEARTBEAT_LOG_EVENT, HeartbeatEvent.class);
		eventMaps.put(BinlogConstants.IGNORABLE_LOG_EVENT, IgnorableEvent.class);
		eventMaps.put(BinlogConstants.ROWS_QUERY_LOG_EVENT, RowsQueryEvent.class);
		eventMaps.put(BinlogConstants.GTID_LOG_EVENT, GtidEvent.class);
		eventMaps.put(BinlogConstants.ANONYMOUS_GTID_LOG_EVENT, AnonymousGtidEvent.class);
		eventMaps.put(BinlogConstants.PREVIOUS_GTIDS_LOG_EVENT, PreviousGtidsEvent.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {

	}

}
