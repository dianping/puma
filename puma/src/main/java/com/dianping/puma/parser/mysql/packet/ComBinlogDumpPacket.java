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
import com.dianping.puma.parser.mysql.MySQLCommunicationConstant;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of ComBinlogDumpPacket
 * 
 * @author Leo Liang
 * 
 */
public class ComBinlogDumpPacket extends AbstractCommandPacket {

	/**
	 * @param command
	 */
	public ComBinlogDumpPacket() {
		super(MySQLCommunicationConstant.COM_BINLOG_DUMP);
	}

	private static final long	serialVersionUID	= 5368054890061442302L;
	private long				binlogPosition;
	private int					binlogFlag;
	private long				serverId;
	private String				binlogFileName;

	public long getBinlogPosition() {
		return binlogPosition;
	}

	public void setBinlogPosition(long binlogPosition) {
		this.binlogPosition = binlogPosition;
	}

	public int getBinlogFlag() {
		return binlogFlag;
	}

	public void setBinlogFlag(int binlogFlag) {
		this.binlogFlag = binlogFlag;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public String getBinlogFileName() {
		return binlogFileName;
	}

	public void setBinlogFileName(String binlogFileName) {
		this.binlogFileName = binlogFileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.server.mysql.packet.AbstractCommandPacket#doBuild(com
	 * .dianping.puma.server.PumaContext)
	 */
	@Override
	protected ByteBuffer doBuild(PumaContext context) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(11 + ((binlogFileName == null || binlogFileName.length() == 0) ? 0
				: binlogFileName.length() * 2));
		PacketUtils.writeByte(buf, command);
		PacketUtils.writeLong(buf, binlogPosition, 4);
		PacketUtils.writeInt(buf, binlogFlag, 2);
		PacketUtils.writeLong(buf, serverId, 4);
		if (binlogFileName != null && binlogFileName.length() != 0) {
			PacketUtils.writeBytesNoNull(buf, binlogFileName.getBytes(context.getEncoding()));
		}
		return buf;
	}

}
