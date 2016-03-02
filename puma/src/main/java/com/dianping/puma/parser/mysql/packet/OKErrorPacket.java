/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-12 $Id$
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
import com.dianping.puma.utils.PacketUtils;
import com.google.common.primitives.UnsignedLong;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of OKErrorPacket
 * 
 * @author Leo Liang
 * 
 */
public class OKErrorPacket extends AbstractResponsePacket {

	private static final long	serialVersionUID	= -1880287868986505141L;
	private static final byte	ERROR_FIELD_COUNT	= (byte) 0xff;
	private static final byte	OK_FIELD_COUNT		= 0x00;

	private boolean				ok					= false;

	private byte				fieldCount;
	private String				message;

	// OK Packet
	private UnsignedLong affectedRows;
	private UnsignedLong		insertId;
	private int					serverStatus;
	private int					warningCount;

	// Error Packet
	private int					errNo;
	private byte				sqlStateMarker;
	private String				sqlState;

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

	public UnsignedLong getAffectedRows() {
		return affectedRows;
	}

	public void setAffectedRows(UnsignedLong affectedRows) {
		this.affectedRows = affectedRows;
	}

	public UnsignedLong getInsertId() {
		return insertId;
	}

	public void setInsertId(UnsignedLong insertId) {
		this.insertId = insertId;
	}

	public int getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(int serverStatus) {
		this.serverStatus = serverStatus;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public void setWarningCount(int warningCount) {
		this.warningCount = warningCount;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.server.mysql.packet.AbstractResponsePacket#doReadPacket
	 * (java.nio.ByteBuffer, com.dianping.puma.server.PumaContext)
	 */
	@Override
	protected void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException {
		fieldCount = buf.get();
		if (ERROR_FIELD_COUNT == fieldCount) {
			errNo = PacketUtils.readInt(buf, 2);
			sqlStateMarker = buf.get();
			sqlState = PacketUtils.readFixedLengthString(buf, 5);
			message = PacketUtils.readFixedLengthString(buf, buf.remaining());
			ok = false;
		} else if (OK_FIELD_COUNT == fieldCount) {
			affectedRows = PacketUtils.readLengthCodedUnsignedLong(buf);
			insertId = PacketUtils.readLengthCodedUnsignedLong(buf);
			serverStatus = PacketUtils.readInt(buf, 2);
			warningCount = PacketUtils.readInt(buf, 2);
			if (buf.hasRemaining()) {
				message = PacketUtils.readFixedLengthString(buf, buf.remaining());
			}
			ok = true;
		}
	}
}
