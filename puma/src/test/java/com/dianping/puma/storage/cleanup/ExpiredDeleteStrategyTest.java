package com.dianping.puma.storage.cleanup;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ExpiredDeleteStrategyTest {
    ExpiredDeleteStrategy target = new ExpiredDeleteStrategy();

    @Test
    public void testNotDate() throws Exception {
        Assert.assertFalse(target.canClean("/data/xxxx"));
    }
}