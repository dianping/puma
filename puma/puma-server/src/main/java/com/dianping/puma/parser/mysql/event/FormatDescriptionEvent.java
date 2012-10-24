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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.utils.PacketUtils;

/**
 * TODO Comment of FormatDescriptionEvent
 * 
 * @author Leo Liang
 * 
 */
public class FormatDescriptionEvent extends AbstractBinlogEvent {
	private static final long	serialVersionUID	= 5209366431892413873L;
	private int					binlogFormatVersion;
	private String				serverVersion;
	private long				createTimestamp;
	private byte				headerLength;
	private byte[]				eventTypes;

	/**
	 * @return the binlogFormatVersion
	 */
	public int getBinlogFormatVersion() {
		return binlogFormatVersion;
	}

	/**
	 * @return the serverVersion
	 */
	public String getServerVersion() {
		return serverVersion;
	}

	/**
	 * @return the createTimestamp
	 */
	public long getCreateTimestamp() {
		return createTimestamp;
	}

	/**
	 * @return the headerLength
	 */
	public byte getHeaderLength() {
		return headerLength;
	}

	/**
	 * @return the eventTypes
	 */
	public byte[] getEventTypes() {
		return eventTypes;
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
		binlogFormatVersion = PacketUtils.readInt(buf, 2);
		serverVersion = PacketUtils.readFixedLengthString(buf, 50);
		createTimestamp = PacketUtils.readLong(buf, 4);
		headerLength = buf.get();
		eventTypes = PacketUtils.readBytes(buf, buf.remaining());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FormatDescriptionEvent [binlogFormatVersion=" + binlogFormatVersion + ", serverVersion="
				+ serverVersion + ", createTimestamp=" + createTimestamp + ", headerLength=" + headerLength
				+ ", eventTypes=" + Arrays.toString(eventTypes) + ", super.toString()=" + super.toString() + "]";
	}

}
