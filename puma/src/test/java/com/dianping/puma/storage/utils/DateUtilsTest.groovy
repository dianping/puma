package com.dianping.puma.storage.utils

import com.dianping.puma.utils.PropertyKeyConstants
import org.junit.Test

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DateUtilsTest extends GroovyTestCase {

    @Override
    void setUp() {
        super.setUp();

        System.setProperty(PropertyKeyConstants.PUMA_DATE_NOW_TYPE, "mock");
        System.setProperty(PropertyKeyConstants.PUMA_DATE_NOW_VALUE, "20150101");
    }

    @Override
    void tearDown() {
        super.tearDown();
        System.clearProperty(PropertyKeyConstants.PUMA_DATE_NOW_TYPE);
    }

    @Test
    public void testGetNextDayWithoutFuture() throws Exception {
        assertEquals(DateUtils.getNextDayWithoutFuture("20141231"), "20150101")
        assertNull(DateUtils.getNextDayWithoutFuture("20150101"))
    }

    @Test
    public void testGetNowCanBeMock() throws Exception {
        assertEquals(DateUtils.getNow().toString("yyyy-MM-dd"), "2015-01-01");
    }

    @Test
    public void testGetNowString() throws Exception {
        assertEquals(DateUtils.getNowString(), "20150101");
    }

    @Test
    public void testGetNowInteger() throws Exception {
        assertEquals(DateUtils.getNowInteger(), 20150101);
    }
}