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
 * accordance with the terms of the license agreement you entered byteo
 * with dianping.com.
 */
package com.dianping.puma.parser.mysql;

/**
 * TODO Comment of BinlogConstanst
 * 
 * @author Leo Liang
 * 
 */
public class BinlogConstanst {
	// event type
	public static final byte	UNKNOWN_EVENT				= 0;
	public static final byte	START_EVENT_V3				= 1;
	public static final byte	QUERY_EVENT					= 2;
	public static final byte	STOP_EVENT					= 3;
	public static final byte	ROTATE_EVENT				= 4;
	public static final byte	byteVAR_EVENT				= 5;
	public static final byte	LOAD_EVENT					= 6;
	public static final byte	SLAVE_EVENT					= 7;
	public static final byte	CREATE_FILE_EVENT			= 8;
	public static final byte	APPEND_BLOCK_EVENT			= 9;
	public static final byte	EXEC_LOAD_EVENT				= 10;
	public static final byte	DELETE_FILE_EVENT			= 11;
	public static final byte	NEW_LOAD_EVENT				= 12;
	public static final byte	RAND_EVENT					= 13;
	public static final byte	USER_VAR_EVENT				= 14;
	public static final byte	FORMAT_DESCRIPTION_EVENT	= 15;
	public static final byte	XID_EVENT					= 16;
	public static final byte	BEGIN_LOAD_QUERY_EVENT		= 17;
	public static final byte	EXECUTE_LOAD_QUERY_EVENT	= 18;
	public static final byte	TABLE_MAP_EVENT				= 19;
	public static final byte	PRE_GA_WRITE_ROWS_EVENT		= 20;
	public static final byte	PRE_GA_UPDATE_ROWS_EVENT	= 21;
	public static final byte	PRE_GA_DELETE_ROWS_EVENT	= 22;
	public static final byte	WRITE_ROWS_EVENT			= 23;
	public static final byte	UPDATE_ROWS_EVENT			= 24;
	public static final byte	DELETE_ROWS_EVENT			= 25;
	public static final byte	INCIDENT_EVENT				= 26;
	public static final byte	HEARTBEAT_LOG_EVENT			= 27;

	// Status variable type
	public static final byte	Q_FLAGS2_CODE				= 0;
	public static final byte	Q_SQL_MODE_CODE				= 1;
	public static final byte	Q_CATALOG_CODE				= 2;
	public static final byte	Q_AUTO_INCREMENT			= 3;
	public static final byte	Q_CHARSET_CODE				= 4;
	public static final byte	Q_TIME_ZONE_CODE			= 5;
	public static final byte	Q_CATALOG_NZ_CODE			= 6;
	public static final byte	Q_LC_TIME_NAMES_CODE		= 7;
	public static final byte	Q_CHARSET_DATABASE_CODE		= 8;
	public static final byte	Q_TABLE_MAP_FOR_UPDATE_CODE	= 9;

}
