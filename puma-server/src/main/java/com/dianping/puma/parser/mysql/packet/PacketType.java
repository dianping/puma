/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-7 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.parser.mysql.packet;

/**
 * TODO Comment of PacketType
 * 
 * @author Leo Liang
 * 
 */
public enum PacketType {
    CONNECT_PACKET, AUTHENTICATE_PACKET, OKERROR_PACKET, COM_BINLOG_DUMP_PACKET, BINLOG_PACKET,QUERY_COMMAND_PACKET;
}
