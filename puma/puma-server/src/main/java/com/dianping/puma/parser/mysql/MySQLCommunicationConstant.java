/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-11 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.parser.mysql;

/**
 * TODO Comment of MySQLConstant
 * 
 * @author Leo Liang
 * 
 */
public final class MySQLCommunicationConstant {
	private MySQLCommunicationConstant() {

	}

	/* new more secure passwords */
	public static final int		CLIENT_LONG_PASSWORD		= 1;
	/* Found instead of affected rows */
	public static final int		CLIENT_FOUND_ROWS			= 2;
	/* Get all column flags */
	public static final int		CLIENT_LONG_FLAG			= 4;
	/* One can specify db on connect */
	public static final int		CLIENT_CONNECT_WITH_DB		= 8;
	/* Don't allow database.table.column */
	public static final int		CLIENT_NO_SCHEMA			= 16;
	/* Can use compression protocol */
	public static final int		CLIENT_COMPRESS				= 32;
	/* Odbc client */
	public static final int		CLIENT_ODBC					= 64;
	/* Can use LOAD DATA LOCAL */
	public static final int		CLIENT_LOCAL_FILES			= 128;
	/* Ignore spaces before '(' */
	public static final int		CLIENT_IGNORE_SPACE			= 256;
	/* New 4.1 protocol */
	public static final int		CLIENT_PROTOCOL_41			= 512;
	/* This is an interactive client */
	public static final int		CLIENT_INTERACTIVE			= 1024;
	/* Switch to SSL after handshake */
	public static final int		CLIENT_SSL					= 2048;
	/* IGNORE sigpipes */
	public static final int		CLIENT_IGNORE_SIGPIPE		= 4096;
	/* Client knows about transactions */
	public static final int		CLIENT_TRANSACTIONS			= 8192;
	/* Old flag for 4.1 protocol */
	public static final int		CLIENT_RESERVED				= 16384;
	/* New 4.1 authentication */
	public static final int		CLIENT_SECURE_CONNECTION	= 32768;
	/* Enable/disable multi-stmt support */
	public static final int		CLIENT_MULTI_STATEMENTS		= 65536;
	/* Enable/disable multi-results */
	public static final int		CLIENT_MULTI_RESULTS		= 131072;

	public static final int		HEADER_LENGTH				= 4;
	public static final int		AUTH_411_OVERHEAD			= 33;

	// Command
	public static final byte	COM_SLEEP					= 0x00;
	public static final byte	COM_QUIT					= 0x01;
	public static final byte	COM_INIT_DB					= 0x02;
	public static final byte	COM_QUERY					= 0x03;
	public static final byte	COM_FIELD_LIST				= 0x04;
	public static final byte	COM_CREATE_DB				= 0x05;
	public static final byte	COM_DROP_DB					= 0x06;
	public static final byte	COM_REFRESH					= 0x07;
	public static final byte	COM_SHUTDOWN				= 0x08;
	public static final byte	COM_STATISTICS				= 0x09;
	public static final byte	COM_PROCESS_INFO			= 0x0a;
	public static final byte	COM_CONNECT					= 0x0b;
	public static final byte	COM_PROCESS_KILL			= 0x0c;
	public static final byte	COM_DEBUG					= 0x0d;
	public static final byte	COM_PING					= 0x0e;
	public static final byte	COM_TIME					= 0x0f;
	public static final byte	COM_DELAYED_INSERT			= 0x10;
	public static final byte	COM_CHANGE_USER				= 0x11;
	public static final byte	COM_BINLOG_DUMP				= 0x12;
	public static final byte	COM_TABLE_DUMP				= 0x13;
	public static final byte	COM_CONNECT_OUT				= 0x14;
	public static final byte	COM_REGISTER_SLAVE			= 0x15;
	public static final byte	COM_STMT_PREPARE			= 0x16;
	public static final byte	COM_STMT_EXECUTE			= 0x17;
	public static final byte	COM_STMT_SEND_LONG_DATA		= 0x18;
	public static final byte	COM_STMT_CLOSE				= 0x19;
	public static final byte	COM_STMT_RESET				= 0x1a;
	public static final byte	COM_SET_OPTION				= 0x1b;
	public static final byte	COM_STMT_FETCH				= 0x1c;
	public static final byte	COM_DAEMON					= 0x1d;
	public static final byte	COM_END						= 0x1e;
}
