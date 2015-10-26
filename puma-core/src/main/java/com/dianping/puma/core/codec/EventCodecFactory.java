/**
 * Project: puma-core
 * <p/>
 * File Created at 2012-7-6
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
package com.dianping.puma.core.codec;

/**
 * @author Leo Liang
 *
 */
public final class EventCodecFactory {
    private EventCodecFactory() {

    }

    public static EventCodec createCodec(String type) {
        if ("raw".equals(type)) {
            return new RawEventCodec();
        }
        return null;
    }
}
