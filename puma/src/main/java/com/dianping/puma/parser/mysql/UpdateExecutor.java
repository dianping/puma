package com.dianping.puma.parser.mysql;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.packet.OKErrorPacket;
import com.dianping.puma.parser.mysql.packet.PacketFactory;
import com.dianping.puma.parser.mysql.packet.PacketType;
import com.dianping.puma.parser.mysql.packet.QueryCommandPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UpdateExecutor {
	
	private InputStream is;

	private OutputStream os;
	
	public UpdateExecutor(InputStream is,OutputStream os){
		this.is=is;
		this.os=os;
	}
	public OKErrorPacket update(String cmd,PumaContext context) throws IOException{
		QueryCommandPacket queryCommandPacket = (QueryCommandPacket) PacketFactory.createCommandPacket(
				PacketType.QUERY_COMMAND_PACKET, context);
		queryCommandPacket.setQueryString(cmd);
		queryCommandPacket.buildPacket(context);
		queryCommandPacket.write(os, context);
		OKErrorPacket okErrorPacket = (OKErrorPacket) PacketFactory.parsePacket(is, PacketType.OKERROR_PACKET,
				context);
		return okErrorPacket;
	}
}
