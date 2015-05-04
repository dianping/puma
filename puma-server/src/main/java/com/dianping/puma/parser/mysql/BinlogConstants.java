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
public final class BinlogConstants {
	private BinlogConstants() {

	}

	// event type
	public static final byte UNKNOWN_EVENT = 0;
	public static final byte START_EVENT_V3 = 1;
	public static final byte QUERY_EVENT = 2;
	public static final byte STOP_EVENT = 3;
	public static final byte ROTATE_EVENT = 4;
	public static final byte INTVAR_EVENT = 5;
	public static final byte LOAD_EVENT = 6;
	public static final byte SLAVE_EVENT = 7;
	public static final byte CREATE_FILE_EVENT = 8;
	public static final byte APPEND_BLOCK_EVENT = 9;
	public static final byte EXEC_LOAD_EVENT = 10;
	public static final byte DELETE_FILE_EVENT = 11;
	public static final byte NEW_LOAD_EVENT = 12;
	public static final byte RAND_EVENT = 13;
	public static final byte USER_VAR_EVENT = 14;
	public static final byte FORMAT_DESCRIPTION_EVENT = 15;
	public static final byte XID_EVENT = 16;
	public static final byte BEGIN_LOAD_QUERY_EVENT = 17;
	public static final byte EXECUTE_LOAD_QUERY_EVENT = 18;
	public static final byte TABLE_MAP_EVENT = 19;
	public static final byte PRE_GA_WRITE_ROWS_EVENT = 20;
	public static final byte PRE_GA_UPDATE_ROWS_EVENT = 21;
	public static final byte PRE_GA_DELETE_ROWS_EVENT = 22;
	public static final byte WRITE_ROWS_EVENT_V1 = 23;
	public static final byte UPDATE_ROWS_EVENT_V1 = 24;
	public static final byte DELETE_ROWS_EVENT_V1 = 25;
	public static final byte INCIDENT_EVENT = 26;
	// mysql 5.6 new add event --start
	public static final byte HEARTBEAT_LOG_EVENT = 27;
	public static final byte IGNORABLE_LOG_EVENT = 28;
	public static final byte ROWS_QUERY_LOG_EVENT = 29;
	public static final byte WRITE_ROWS_EVENT = 30;
	public static final byte UPDATE_ROWS_EVENT = 31;
	public static final byte DELETE_ROWS_EVENT = 32;
	public static final byte GTID_LOG_EVENT = 33;
	public static final byte ANONYMOUS_GTID_LOG_EVENT = 34;
	public static final byte PREVIOUS_GTIDS_LOG_EVENT = 35;
	// --end
	// Status variable type
	public static final byte Q_FLAGS2_CODE = 0;
	public static final byte Q_SQL_MODE_CODE = 1;
	public static final byte Q_CATALOG_CODE = 2;
	public static final byte Q_AUTO_INCREMENT = 3;
	public static final byte Q_CHARSET_CODE = 4;
	public static final byte Q_TIME_ZONE_CODE = 5;
	public static final byte Q_CATALOG_NZ_CODE = 6;
	public static final byte Q_LC_TIME_NAMES_CODE = 7;
	public static final byte Q_CHARSET_DATABASE_CODE = 8;
	public static final byte Q_TABLE_MAP_FOR_UPDATE_CODE = 9;

	// User variable type
	public static final byte STRING_RESULT = 0;
	public static final byte REAL_RESULT = 1;
	public static final byte INT_RESULT = 2;
	public static final byte ROW_RESULT = 3;
	public static final byte DECIMAL_RESULT = 4;

	// Mysql column data type
	public static final int MYSQL_TYPE_DECIMAL = 0;
	public static final int MYSQL_TYPE_TINY = 1;
	public static final int MYSQL_TYPE_SHORT = 2;
	public static final int MYSQL_TYPE_INT = 3;
	public static final int MYSQL_TYPE_FLOAT = 4;
	public static final int MYSQL_TYPE_DOUBLE = 5;
	public static final int MYSQL_TYPE_NULL = 6;
	public static final int MYSQL_TYPE_TIMESTAMP = 7;
	public static final int MYSQL_TYPE_LONGLONG = 8;
	public static final int MYSQL_TYPE_INT24 = 9;
	public static final int MYSQL_TYPE_DATE = 10;
	public static final int MYSQL_TYPE_TIME = 11;
	public static final int MYSQL_TYPE_DATETIME = 12;
	public static final int MYSQL_TYPE_YEAR = 13;
	public static final int MYSQL_TYPE_NEWDATE = 14;
	public static final int MYSQL_TYPE_VARCHAR = 15;
	public static final int MYSQL_TYPE_BIT = 16;
	// mysql 5.6 new add type --start
	public static final int MYSQL_TYPE_TIMESTAMP2 = 17;
	public static final int MYSQL_TYPE_DATETIME2 = 18;
	public static final int MYSQL_TYPE_TIME2 = 19;
	// --end
	public static final int MYSQL_TYPE_NEWDECIMAL = 246;
	public static final int MYSQL_TYPE_ENUM = 247;
	public static final int MYSQL_TYPE_SET = 248;
	public static final int MYSQL_TYPE_TINY_BLOB = 249;
	public static final int MYSQL_TYPE_MEDIUM_BLOB = 250;
	public static final int MYSQL_TYPE_LONG_BLOB = 251;
	public static final int MYSQL_TYPE_BLOB = 252;
	public static final int MYSQL_TYPE_VAR_STRING = 253;
	public static final int MYSQL_TYPE_STRING = 254;
	public static final int MYSQL_TYPE_GEOMETRY = 255;
	// checksum
	public static final int CHECKSUM_ALG_OFF = 0;
	public static final int CHECKSUM_ALG_CRC32 = 1;
	public static final int CHECKSUM_ALG_ENUM_END = 2;
	public static final int CHECKSUM_ALG_UNDEF = 255;
	public static final int CHECKSUM_CRC32_SIGNATURE_LEN = 4;
	public static final int CHECKSUM_ALG_DESC_LEN = 1;
	public static final int CHECKSUM_LEN = CHECKSUM_CRC32_SIGNATURE_LEN;
	public static final int[] checksumVersionSplit = { 5, 6, 1 };
	public static final long checksumVersionProduct = (checksumVersionSplit[0] * 256 + checksumVersionSplit[1]) * 256
			+ checksumVersionSplit[2];

}
