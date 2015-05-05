package com.dianping.puma.parser.mysql.event;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.utils.PacketUtils;

public class RowsQueryEvent extends AbstractBinlogEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3869282663038890813L;
	private String rowsQuery;

	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		int lenRemaining = context.isCheckSum() ? buf.remaining() - 4 : buf.remaining();
		rowsQuery = PacketUtils.readFixedLengthString(buf, lenRemaining);
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
