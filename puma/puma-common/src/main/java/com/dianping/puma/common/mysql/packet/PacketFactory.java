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
package com.dianping.puma.common.mysql.packet;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.dianping.puma.common.bo.PumaContext;

/**
 * 
 * @author Leo Liang
 * 
 */
public class PacketFactory {

	private static final Logger	log	= Logger.getLogger(PacketFactory.class);

	public static ResponsePacket parsePacket(InputStream is, PacketType packetType, PumaContext context)
			throws IOException {
		try {
			switch (packetType) {
				case CONNECT_PACKET:
					ResponsePacket greetingPacket = new ConnectPacket();
					greetingPacket.readPacket(is, context);
					return greetingPacket;
				case OKERROR_PACKET:
					OKErrorPacket okErrorPacket = new OKErrorPacket();
					okErrorPacket.readPacket(is, context);
					return okErrorPacket;
				case BINLOG_PACKET:
					BinlogPacket binlogPacket = new BinlogPacket();
					binlogPacket.readPacket(is, context);
					return binlogPacket;
				default:
					return null;
			}
		} catch (IOException e) {
			log.error("Read packet failed.", e);
			throw e;
		}
	}

	public static CommandPacket createCommandPacket(PacketType packetType, PumaContext context) {
		switch (packetType) {
			case AUTHENTICATE_PACKET:
				AuthenticatePacket authenticatePacket = new AuthenticatePacket();
				authenticatePacket.setSeed(context.getSeed());
				authenticatePacket.setSeq(1);
				return authenticatePacket;
			case COM_BINLOG_DUMP_PACKET:
				ComBinlogDumpPacket comBinlogDumpPacket = new ComBinlogDumpPacket();
				comBinlogDumpPacket.setSeq(0);
				return comBinlogDumpPacket;
			default:
				return null;
		}
	}

}