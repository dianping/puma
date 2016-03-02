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
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of AbstractBinlogEvent
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractBinlogEvent implements BinlogEvent {
	private static final long serialVersionUID = -8136236885229956889L;
	private BinlogHeader header;
	private int checksumAlg = BinlogConstants.CHECKSUM_ALG_OFF;
	private long crc;

	@Override
	public void parse(ByteBuffer buf, PumaContext context, BinlogHeader header) throws IOException {
		this.header = header;
		doParse(buf, context);
		if (!(this.header.getEventType() == BinlogConstants.ROTATE_EVENT)) {
			checksumAlg = context.getChecksumAlg(); // fetch checksum alg
			parseCheckSum(buf);
		}
	}

	@Override
	public BinlogHeader getHeader() {
		return header;
	}
	
	@Override
	public void setHeader(BinlogHeader header) {
		this.header = header;
	}

	public abstract void doParse(ByteBuffer buf, PumaContext context) throws IOException;

	private void parseCheckSum(ByteBuffer buf) {
		if (checksumAlg != BinlogConstants.CHECKSUM_ALG_OFF && checksumAlg != BinlogConstants.CHECKSUM_ALG_UNDEF) {
			buf.position((int) (this.header.getEventLength() - 4));
			setCrc(PacketUtils.readLong(buf, 4));
		}
	}

	@Override public String toString() {
		return new ToStringBuilder(this)
				.append("header", header)
				.append("checksumAlg", checksumAlg)
				.append("crc", crc)
				.toString();
	}

	public void setChecksumAlg(int checksumAlg) {
		this.checksumAlg = checksumAlg;
	}

	public int getChecksumAlg() {
		return checksumAlg;
	}

	public long getCrc() {
		return crc;
	}

	public void setCrc(long crc) {
		this.crc = crc;
	}

	public boolean isRemaining(ByteBuffer buf, PumaContext context) {
		return context.isCheckSum() ? buf.remaining() - 4 > 0 : buf.hasRemaining();
	}

	public int lenRemaining(ByteBuffer buf, PumaContext context) {
		return context.isCheckSum() ? buf.remaining() - 4 : buf.remaining();
	}
}
