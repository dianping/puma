package com.dianping.puma.api.cleanup

import org.junit.Test

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
class CleanUpHelperTest {
    @Test
    public void testCleanUp() throws Exception {
        Object item = new Object();

        final AtomicBoolean hasClean = new AtomicBoolean();

        CleanUpHelper.register(item, new CleanUp() {
            @Override
            void cleanUp() {
                hasClean.set(true);
            }
        })

        assert !hasClean.get()
        item = null;
        System.gc();

        Thread.sleep(200);

        assert hasClean.get()
    }
}
