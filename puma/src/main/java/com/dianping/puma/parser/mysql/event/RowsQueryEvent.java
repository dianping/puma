package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RowsQueryEvent extends AbstractBinlogEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3869282663038890813L;
	private String rowsQuery;

	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		int lenRemaining = lenRemaining(buf, context);
		rowsQuery = PacketUtils.readFixedLengthString(buf, lenRemaining, PacketUtils.UTF_8);
	}

	public void setRowsQuery(String rowsQuery) {
		this.rowsQuery = rowsQuery;
	}

	public String getRowsQuery() {
		return rowsQuery;
	}

	@Override
	public String toString() {
		return "RowsQueryEvent [ rowsQuery= " + this.rowsQuery + " , super.toString()= " + super.toString() + "]";
	}
}
