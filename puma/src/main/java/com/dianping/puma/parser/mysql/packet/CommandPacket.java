/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-7 $Id$
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO Comment of RequestPacket
 * 
 * @author Leo Liang
 * 
 */
public interface CommandPacket extends Packet {

	byte[] getBytes();

	void buildPacket(PumaContext context) throws IOException;

	void write(OutputStream os, PumaContext context) throws IOException;
}
