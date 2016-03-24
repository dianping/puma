package com.dianping.puma.api.impl;

import com.dianping.puma.log.LoggerLoader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class LogTest {
    @Test
    public void testLog() throws Exception {
        LoggerLoader.init();
        Logger eventLogger = LoggerFactory.getLogger("PumaClientEventLogger");
        eventLogger.info("12ssdsssd");
    }
}
