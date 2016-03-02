package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;
import com.google.common.primitives.UnsignedLong;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RSHeaderPacket extends AbstractResponsePacket {

	private static final long serialVersionUID = -7038373881010765819L;

	private boolean ok;
	private byte fieldCount;
	private String message;

	// Error Packet
	private int errNo;
	private byte sqlStateMarker;
	private String sqlState;

	// RSHeaderPacket
	private UnsignedLong columnCount;
	private UnsignedLong extra;
	
	@Override
	protected void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException {
		fieldCount = buf.get();
		if (fieldCount < 0) {
			errNo = PacketUtils.readInt(buf, 2);
			sqlStateMarker = buf.get();
			sqlState = PacketUtils.readFixedLengthString(buf, 5);
			message = PacketUtils.readFixedLengthString(buf, buf.remaining());
			ok = false;
		} else {
			buf.position(0);
			columnCount =PacketUtils.readLengthCodedUnsignedLong(buf); 
			if(buf.hasRemaining()){
				extra = PacketUtils.readLengthCodedUnsignedLong(buf);
			}
			ok = true;
		}

	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public byte getFieldCount() {
		return fieldCount;
	}

	public void setFieldCount(byte fieldCount) {
		this.fieldCount = fieldCount;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getErrNo() {
		return errNo;
	}

	public void setErrNo(int errNo) {
		this.errNo = errNo;
	}

	public byte getSqlStateMarker() {
		return sqlStateMarker;
	}

	public void setSqlStateMarker(byte sqlStateMarker) {
		this.sqlStateMarker = sqlStateMarker;
	}

	public String getSqlState() {
		return sqlState;
	}

	public void setSqlState(String sqlState) {
		this.sqlState = sqlState;
	}

	public UnsignedLong getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(UnsignedLong columnCount) {
		this.columnCount = columnCount;
	}

	public UnsignedLong getExtra() {
		return extra;
	}

	public void setExtra(UnsignedLong extra) {
		this.extra = extra;
	}
}
