/**
 * Project: ${puma-parser.aid}
 * 
 * File Created at 2012-6-24
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.variable.user.*;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of UserVarEvent
 * 
 * @author Leo Liang
 * 
 */
public class UserVarEvent extends AbstractBinlogEvent {
	private static final long	serialVersionUID	= 4941407067308562104L;
	private int					varNameLength;
	private String				varName;
	private boolean				sqlNull;
	private byte				varType;
	private int					varCollation;
	private int					varValueLength;
	private UserVariable		varValue;

	/**
	 * @return the varNameLength
	 */
	public int getVarNameLength() {
		return varNameLength;
	}

	/**
	 * @return the varName
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * @return the sqlNull
	 */
	public boolean isSqlNull() {
		return sqlNull;
	}

	/**
	 * @return the varType
	 */
	public byte getVarType() {
		return varType;
	}

	/**
	 * @return the varCollation
	 */
	public int getVarCollation() {
		return varCollation;
	}

	/**
	 * @return the varValueLength
	 */
	public int getVarValueLength() {
		return varValueLength;
	}

	/**
	 * @return the varValue
	 */
	public UserVariable getVarValue() {
		return varValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.parser.mysql.event.AbstractBinlogEvent#doParse(java
	 * .nio.ByteBuffer, com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		varNameLength = PacketUtils.readInt(buf, 4);
		varName = PacketUtils.readFixedLengthString(buf, varNameLength);
		sqlNull = buf.get() == 0x00 ? false : true;
		if (!sqlNull) {
			varType = buf.get();
			varCollation = PacketUtils.readInt(buf, 4);
			varValueLength = PacketUtils.readInt(buf, 4);
			varValue = parseUserVariable(buf);
		}

	}

	protected UserVariable parseUserVariable(ByteBuffer buf) throws IOException {
		switch (varType) {
			case BinlogConstants.DECIMAL_RESULT:
				return new UserVariableDecimal(PacketUtils.readBytes(buf, varValueLength));
			case BinlogConstants.INT_RESULT:
				return new UserVariableInt(PacketUtils.readLong(buf, varValueLength));
			case BinlogConstants.REAL_RESULT:
				return new UserVariableReal(Double.longBitsToDouble(PacketUtils.readLong(buf, varValueLength)));
			case BinlogConstants.ROW_RESULT:
				return new UserVariableRow(PacketUtils.readBytes(buf, varValueLength));
			case BinlogConstants.STRING_RESULT:
				return new UserVariableString(PacketUtils.readBytes(buf, varValueLength), varCollation);
			default:
				return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserVarEvent [varNameLength=" + varNameLength + ", varName=" + varName + ", sqlNull=" + sqlNull
				+ ", varType=" + varType + ", varCollation=" + varCollation + ", varValueLength=" + varValueLength
				+ ", varValue=" + varValue + ", super.toString()=" + super.toString() + "]";
	}
}
