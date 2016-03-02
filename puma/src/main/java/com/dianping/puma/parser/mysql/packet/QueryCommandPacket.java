package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.MySQLCommunicationConstant;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class QueryCommandPacket extends AbstractCommandPacket {

	private static final long serialVersionUID = -1251895157791360027L;

	private String queryString;

	public QueryCommandPacket() {
		super(MySQLCommunicationConstant.COM_QUERY);
	}

	@Override
	protected ByteBuffer doBuild(PumaContext context) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(1 + ((queryString == null || queryString.length() == 0) ? 0 : queryString
				.length() * 2));
		PacketUtils.writeByte(buf, command);
		if(!(queryString == null || queryString.length() == 0)){
			PacketUtils.writeBytesNoNull(buf, queryString.getBytes(context.getEncoding()));
		}
		return buf;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

}
