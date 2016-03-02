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
import java.io.InputStream;

/**
 * TODO Comment of ResponsePacket
 * 
 * @author Leo Liang
 * 
 */
public interface ResponsePacket extends Packet {

	void readPacket(InputStream is, PumaContext context) throws IOException;
}
