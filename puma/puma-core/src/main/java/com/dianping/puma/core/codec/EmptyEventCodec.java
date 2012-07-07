/**
 * Project: puma-core
 * 
 * File Created at 2012-7-7
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
package com.dianping.puma.core.codec;

import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;

/**
 * TODO Comment of EmptyEventCodec
 * 
 * @author Leo Liang
 * 
 */
public class EmptyEventCodec implements EventCodec {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.core.codec.EventCodec#encode(com.dianping.puma.core
	 * .event.ChangedEvent)
	 */
	@Override
	public byte[] encode(ChangedEvent event) throws IOException {
		return new byte[1024];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.core.codec.EventCodec#decode(byte[])
	 */
	@Override
	public ChangedEvent decode(byte[] data) throws IOException {
		// TODO Auto-generated method stub
		return new DdlEvent();
	}

}
