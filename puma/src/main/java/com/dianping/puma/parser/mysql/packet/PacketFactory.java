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

import com.dianping.puma.common.PumaContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Leo Liang
 * 
 */
public final class PacketFactory {
	private PacketFactory() {

	}

	public static ResponsePacket parsePacket(InputStream is, PacketType packetType, PumaContext context)
			throws IOException {
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
		case RSHEADER_PACKET:
			RSHeaderPacket rsHeaderPacket=new RSHeaderPacket();
			rsHeaderPacket.readPacket(is, context);
			return rsHeaderPacket;
		case FIELD_PACKET:
			FieldPacket feildPacket=new FieldPacket();
			feildPacket.readPacket(is, context);
			return feildPacket;
		case ROWDATA_PACKET:
			RowDataPacket rowDataPacket=new RowDataPacket();
			rowDataPacket.readPacket(is, context);
			return rowDataPacket;
		case EOF_PACKET:
			EofPacket eofPacket=new EofPacket();
			eofPacket.readPacket(is, context);
			return eofPacket;
		default:
			return null;
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
		case QUERY_COMMAND_PACKET:
			QueryCommandPacket queryCommandPacket = new QueryCommandPacket();
			return queryCommandPacket;
		default:
			return null;
		}
	}

}
