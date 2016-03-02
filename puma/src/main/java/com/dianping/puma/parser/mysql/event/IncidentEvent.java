/**
 * Project: ${puma-common.aid}
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
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of IncidentEvent
 * 
 * @author Leo Liang
 * 
 */
public class IncidentEvent extends AbstractBinlogEvent {

	private static final long	serialVersionUID	= -3512326038882986688L;
	private int					incidentNumber;
	private int					messageLength;
	private String				message;

	/**
	 * @return the incidentNumber
	 */
	public int getIncidentNumber() {
		return incidentNumber;
	}

	/**
	 * @return the messageLength
	 */
	public int getMessageLength() {
		return messageLength;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IncidentEvent [incidentNumber=" + incidentNumber + ", messageLength=" + messageLength + ", message="
				+ message + ", super.toString()=" + super.toString() + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.common.mysql.event.AbstractBinlogEvent#doParse(java
	 * .nio.ByteBuffer, com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		incidentNumber = PacketUtils.readInt(buf, 1);
		messageLength = PacketUtils.readInt(buf, 1);
		if (messageLength > 0) {
			message = PacketUtils.readFixedLengthString(buf, messageLength);
		}
	}

}
