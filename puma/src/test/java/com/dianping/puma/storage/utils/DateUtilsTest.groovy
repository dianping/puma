package com.dianping.puma.storage.utils

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
        DateUtils.changeGetNowTime("20150101");
    }

    @Override
    void tearDown() {
        super.tearDown();
        DateUtils.changeGetNowTime(null);
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

    @Test
    void testExpired() {
        assertTrue(DateUtils.expired("20151110", "20151120", 5))
        assertFalse(DateUtils.expired("20151119", "20151120", 5))
    }
}