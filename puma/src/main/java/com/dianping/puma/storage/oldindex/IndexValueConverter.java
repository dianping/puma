package com.dianping.puma.storage.oldindex;

import com.dianping.puma.storage.Sequence;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class IndexValueConverter implements IndexItemConverter<IndexValueImpl> {

	private IndexKeyConverter indexKeyConvertor = new IndexKeyConverter();

	@Override
	public IndexValueImpl convertFromObj(Object value) {
		byte[] bytes = (byte[]) value;
		ByteBuf buf = Unpooled.wrappedBuffer(bytes);
		IndexValueImpl l2Index = new IndexValueImpl();

		int len = buf.readByte();

		byte[] keyBytes = new byte[len];

		buf.readBytes(keyBytes);
		IndexKeyImpl indexKey = indexKeyConvertor.convertFromObj(new String(keyBytes));

		l2Index.setIndexKey(indexKey);

		byte[] filter = new byte[4];
		buf.readBytes(filter);

		l2Index.setDdl(filter[0] == 1);
		l2Index.setDml(filter[1] == 1);
		l2Index.setTransactionBegin(filter[2] == 1);
		l2Index.setTransactionCommit(filter[3] == 1);

		Sequence sequence = new Sequence(buf.readLong(), buf.readInt());
		l2Index.setSequence(sequence);

		return l2Index;
	}

	@Override
	public byte[] convertToObj(IndexValueImpl value) {
		ByteBuf buf = Unpooled.buffer();

		String binlogIndexKey = indexKeyConvertor.convertToObj(value.getIndexKey());
		byte[] binlogIndexKeyBytes = binlogIndexKey.getBytes();
		buf.writeByte(binlogIndexKey.length());
		buf.writeBytes(binlogIndexKeyBytes);

		byte[] filter = new byte[4];

		filter[0] = (byte) (value.isDdl() ? 1 : 0);
		filter[1] = (byte) (value.isDml() ? 1 : 0);
		filter[2] = (byte) (value.isTransactionBegin() ? 1 : 0);
		filter[3] = (byte) (value.isTransactionCommit() ? 1 : 0);

		buf.writeBytes(filter);

		buf.writeLong(value.getSequence().longValue());
		buf.writeInt(value.getSequence().getLen());

		int readableBytes = buf.readableBytes();

		byte[] readableBytesArray = new byte[readableBytes];
		buf.readBytes(readableBytesArray);

		return readableBytesArray;
	}
}
