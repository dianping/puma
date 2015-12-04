package com.dianping.puma.storage.maintain.clean;

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ExpiredDeleteStrategyTest {
    ExpiredDeleteStrategy target = new ExpiredDeleteStrategy();

    @Test
    public void testNotDate() throws Exception {
        Assert.assertFalse(target.canClean(new File("/data/xxxx")));
    }
}