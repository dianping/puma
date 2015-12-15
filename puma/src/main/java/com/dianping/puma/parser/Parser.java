/**
 * Project: ${puma-parser.aid}
 * <p/>
 * File Created at 2012-6-23
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.parser;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.event.BinlogEvent;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of Parser
 *
 * @author Leo Liang
 *
 */
public interface Parser extends LifeCycle {
    BinlogEvent parse(ByteBuffer buf, PumaContext context) throws IOException;
}
