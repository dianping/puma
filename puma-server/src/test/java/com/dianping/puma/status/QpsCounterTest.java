package com.dianping.puma.status;

import org.junit.Assert;
import org.junit.Test;

/**
 * Dozer @ 7/22/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class QpsCounterTest {
    @Test
    public void test_default() throws Exception {
        QpsCounter target = new QpsCounter();
        target.increase();
        target.increase();
        target.increase();
        target.increase();
        target.increase();

        Assert.assertEquals(5, target.get());
    }

    @Test
    public void test_get_more_second() throws Exception {
        QpsCounter target = new QpsCounter(60);
        target.increase();
        target.increase();
        target.increase();
        target.increase();
        target.increase();
        Assert.assertEquals(5, target.get());
        Thread.sleep(500);
        Assert.assertEquals(5, target.get());
        Thread.sleep(500);
        target.increase();
        Assert.assertEquals(1, target.get());
        Assert.assertEquals(3, target.get(2));
    }
}