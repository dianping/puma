/**
 * Project: ${puma-parser.aid}
 * 
 * File Created at 2012-6-23
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
import com.dianping.puma.utils.PacketUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * TODO Comment of BinlogHeader
 * 
 * @author Leo Liang
 * 
 */
public class BinlogHeader implements Serializable {

	private static final long serialVersionUID = 5056491879587690096L;

	private long timestamp;

	private byte eventType;

	private long serverId;

	private long eventLength;

	private long nextPosition;

	private int flags;

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *           the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the eventType
	 */
	public byte getEventType() {
		return eventType;
	}

	/**
	 * @param eventType
	 *           the eventType to set
	 */
	public void setEventType(byte eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the serverId
	 */
	public long getServerId() {
		return serverId;
	}

	/**
	 * @param serverId
	 *           the serverId to set
	 */
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the eventLength
	 */
	public long getEventLength() {
		return eventLength;
	}

	/**
	 * @param eventLength
	 *           the eventLength to set
	 */
	public void setEventLength(long eventLength) {
		this.eventLength = eventLength;
	}

	/**
	 * @return the nextPosition
	 */
	public long getNextPosition() {
		return nextPosition;
	}

	/**
	 * @param nextPosition
	 *           the nextPosition to set
	 */
	public void setNextPosition(long nextPosition) {
		this.nextPosition = nextPosition;
	}

	/**
	 * @return the flags
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * @param flags
	 *           the flags to set
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("timestamp", timestamp).append("eventType", eventType)
		      .append("serverId", serverId).append("eventLength", eventLength).append("nextPosition", nextPosition)
		      .append("flags", flags).toString();
	}

	public void parse(ByteBuffer buf, PumaContext context) {
		timestamp = PacketUtils.readLong(buf, 4);
		eventType = buf.get();
		serverId = PacketUtils.readLong(buf, 4);
		eventLength = PacketUtils.readLong(buf, 4);
		nextPosition = PacketUtils.readLong(buf, 4);
		flags = PacketUtils.readInt(buf, 2);
	}

}
