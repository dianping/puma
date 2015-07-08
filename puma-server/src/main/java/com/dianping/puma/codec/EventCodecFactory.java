/**
 * Project: puma-core
 * 
 * File Created at 2012-7-6
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
package com.dianping.puma.codec;

/**
 * TODO Comment of EventCodecFactory
 * 
 * @author Leo Liang
 * 
 */
public final class EventCodecFactory {
	private EventCodecFactory() {

	}

	public static EventCodec createCodec(String type) {
		if ("json".equals(type)) {
			return new JsonEventCodec();
		}
		return null;
	}
}
