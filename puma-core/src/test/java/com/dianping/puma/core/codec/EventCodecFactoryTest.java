/**
 * Project: puma-core
 * <p/>
 * File Created at 2012-7-27
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

import junit.framework.Assert;
import org.junit.Test;


/**
 * TODO Comment of EventCodecFactoryTest
 *
 * @author Leo Liang
 *
 */
public class EventCodecFactoryTest {
    @Test
    public void createCodecTest() {
        EventCodec codec = EventCodecFactory.createCodec("raw");
        Assert.assertNotNull(codec);
        Assert.assertEquals(RawEventCodec.class, codec.getClass());
    }

}
