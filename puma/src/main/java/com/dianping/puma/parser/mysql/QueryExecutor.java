package com.dianping.puma.parser.mysql;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.packet.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {

	private InputStream is;

	private OutputStream os;

	public QueryExecutor(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}

	/*
	 * (Result Set Header Packet) the number of columns <br> 
	 * (Field Packets)column descriptors <br> 
	 * (EOF Packet) marker: end of Field Packets <br>
	 * (Row Data Packets) row contents <br>
	 * (EOF Packet) marker: end of Data Packets
	 */
	public ResultSet query(String cmd, PumaContext context) throws IOException {

		QueryCommandPacket queryCommandPacket = (QueryCommandPacket) PacketFactory.createCommandPacket(
				PacketType.QUERY_COMMAND_PACKET, context);
		queryCommandPacket.setQueryString(cmd);
		queryCommandPacket.buildPacket(context);
		queryCommandPacket.write(os, context);
		RSHeaderPacket rsHeaderPacket = (RSHeaderPacket) PacketFactory.parsePacket(is, PacketType.RSHEADER_PACKET,
				context);
		if (!rsHeaderPacket.isOk()) {
			throw new IOException("queryConfig failed. Reason: " + rsHeaderPacket.getMessage());
		}
		List<FieldPacket> fields = new ArrayList<FieldPacket>();
		for (int i = 0; i < rsHeaderPacket.getColumnCount().intValue(); i++) {
			FieldPacket fieldPacket = (FieldPacket) PacketFactory.parsePacket(is, PacketType.FIELD_PACKET, context);
			fields.add(fieldPacket);
		}
		EofPacket eofPacket = (EofPacket) PacketFactory.parsePacket(is, PacketType.EOF_PACKET, context);
		if (!eofPacket.isOk()) {
			throw new IOException("queryConfig failed. Reason: EofPacket exception.");
		}
		List<RowDataPacket> rowDataPackets = new ArrayList<RowDataPacket>();
		RowDataPacket rowDataPacket = null;
		do {
			if (rowDataPacket != null) {
				rowDataPackets.add(rowDataPacket);
			}
			rowDataPacket = (RowDataPacket) PacketFactory.parsePacket(is, PacketType.ROWDATA_PACKET, context);
		} while (rowDataPacket.isOk());

		ResultSet resultSet = new ResultSet();
		resultSet.getFieldDescriptors().addAll(fields);
		for (RowDataPacket r : rowDataPackets) {
			resultSet.getFiledValues().addAll(r.getColumns());
		}
		return resultSet;
	}
}
