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
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * TODO Comment of FormatDescriptionEvent
 * 
 * @author Leo Liang
 * 
 */
public class FormatDescriptionEvent extends AbstractBinlogEvent {
	private static final long serialVersionUID = 5209366431892413873L;
	private int binlogFormatVersion;
	private String serverVersion;
	private long createTimestamp;
	private byte headerLength;
	private byte[] eventTypes;

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
		eventTypes = PacketUtils.readBytes(buf, buf.remaining() - 5);
		int versionSplit[] = new int[] { 0, 0, 0 };
		doServerVersionSplit(serverVersion, versionSplit);
		if (versionProduct(versionSplit) >= BinlogConstants.checksumVersionProduct) {
			this.setChecksumAlg(PacketUtils.readInt(buf, 1));
		}
		context.setChecksumAlg(this.getChecksumAlg());
		context.setServerVersion(serverVersion);
	}

	private void doServerVersionSplit(String serverVersion, int[] versionSplit) {
		String[] split = serverVersion.split("\\.");
		if (split.length < 3) {
			versionSplit[0] = 0;
			versionSplit[1] = 0;
			versionSplit[2] = 0;
		} else {
			int j = 0;
			for (int i = 0; i <= 2; i++) {
				String str = split[i];
				for (j = 0; j < str.length(); j++) {
					if (Character.isDigit(str.charAt(j)) == false) {
						break;
					}
				}
				if (j > 0) {
					versionSplit[i] = Integer.valueOf(str.substring(0, j), 10);
				} else {
					versionSplit[0] = 0;
					versionSplit[1] = 0;
					versionSplit[2] = 0;
				}
			}
		}
	}

	private long versionProduct(int[] versionSplit) {
		return ((versionSplit[0] * 256 + versionSplit[1]) * 256 + versionSplit[2]);
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
