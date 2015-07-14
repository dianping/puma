package com.dianping.puma.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class L2IndexItemConvertor implements IndexItemConvertor<L2Index> {

	private BinlogIndexKeyConvertor binlogIndexKeyConvertor = new BinlogIndexKeyConvertor();

	@Override
	public L2Index convertFromObj(Object value) {
		byte[] bytes = (byte[]) value;
		ByteBuf buf = Unpooled.wrappedBuffer(bytes);
		L2Index l2Index = new L2Index();

		int len = buf.readByte();

		byte[] keyBytes = new byte[len];

		buf.readBytes(keyBytes);
		BinlogIndexKey binlogIndexKey = binlogIndexKeyConvertor.convertFromObj(new String(keyBytes));

		l2Index.setBinlogIndexKey(binlogIndexKey);

		buf.readChar(); // skip '='

		int databaseLen = buf.readByte();
		if (databaseLen > 0) {
			byte[] database = new byte[databaseLen];
			buf.readBytes(database);

			l2Index.setDatabase(new String(database));
		}

		buf.readChar(); // skip '.'

		int tableLen = buf.readByte();
		if (tableLen > 0) {
			byte[] table = new byte[tableLen];
			buf.readBytes(table);

			l2Index.setTable(new String(table));
		}

		buf.readChar(); // skip '+'
		
		byte[] filter = new byte[8]; // 预留8位给其他filter，目前只用2位
		buf.readBytes(filter);

		l2Index.setDdl(filter[0] == 1);
		l2Index.setDml(filter[1] == 1);

		buf.readChar(); // skip '+'
		Sequence sequence = new Sequence(buf.readLong());
		l2Index.setSequence(sequence);

		return l2Index;
	}

	@Override
	public byte[] convertToObj(L2Index value) {
		ByteBuf buf = Unpooled.buffer();

		String binlogIndexKey = binlogIndexKeyConvertor.convertToObj(value.getBinlogIndexKey());
		byte[] binlogIndexKeyBytes = binlogIndexKey.getBytes();
		buf.writeByte(binlogIndexKey.length());
		buf.writeBytes(binlogIndexKeyBytes);
		buf.writeChar('=');

		String database = value.getDatabase();
		if (database != null && database.length() > 0) {
			byte[] databaseBytes = database.getBytes();

			buf.writeByte(databaseBytes.length);
			buf.writeBytes(databaseBytes);
		} else {
			buf.writeByte(0);
		}

		buf.writeChar('.');

		String table = value.getTable();
		if (table != null && table.length() > 0) {
			byte[] tableBytes = table.getBytes();

			buf.writeByte(tableBytes.length);
			buf.writeBytes(tableBytes);
		} else {
			buf.writeByte(0);
		}

		buf.writeChar('+');

		
		byte[] filter = new byte[8];

		filter[0] = (byte) (value.isDdl() ? 1 : 0);
		filter[1] = (byte) (value.isDml() ? 1 : 0);
		for (int i = 2; i < 8; i++) {
			filter[i] = 0;
		}

		buf.writeBytes(filter);
		buf.writeChar('+');

		buf.writeLong(value.getSequence().longValue());

		int readableBytes = buf.readableBytes();

		byte[] readableBytesArray = new byte[readableBytes];
		buf.readBytes(readableBytesArray);

		return readableBytesArray;
	}
}
