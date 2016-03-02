/**
 * Project: ${puma-parser.aid}
 * <p/>
 * File Created at 2012-6-24
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.parser;

import com.dianping.puma.annotation.ThreadSafe;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO Comment of DefaultBinlogParser
 *
 * @author Leo Liang
 */
@ThreadSafe
public class DefaultBinlogParser implements Parser {
    private final Logger logger = LoggerFactory.getLogger(DefaultBinlogParser.class);
    private static Map<Byte, Class<? extends BinlogEvent>> eventMaps = new ConcurrentHashMap<Byte, Class<? extends BinlogEvent>>();

    @Override
    public BinlogEvent parse(ByteBuffer buf, PumaContext context) throws IOException {

        logger.debug("\n\n\n");
        logger.debug("****************************** binlog parse begin ******************************");

        BinlogHeader header = new BinlogHeader();
        header.parse(buf, context);

        logger.debug("binlog event header:\n");
        logger.debug("{}", header);

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

        logger.debug("binlog event type:\n");
        logger.debug("{}", event.getClass());

        event.parse(buf, context, header);

        logger.debug("binlog event:\n");
        logger.debug("{}", event);
        logger.debug("****************************** binlog parse end ******************************");
        logger.debug("\n\n\n");

        return event;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dianping.puma.common.LifeCycle#start()
     */
    @Override
    public void start() {
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

    @Override
    public void stop() {

    }

}
