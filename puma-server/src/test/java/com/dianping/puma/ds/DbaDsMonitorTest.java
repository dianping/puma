package com.dianping.puma.ds;

import com.google.gson.Gson;
import org.junit.Test;

/**
 * Dozer @ 8/11/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DbaDsMonitorTest {
    @Test
    public void test_read_cluster() throws Exception {
        DbaDsMonitor dsMonitor = new DbaDsMonitor();
        dsMonitor.scheduleDbaQuery();
        System.out.println(new Gson().toJson(dsMonitor.clusters));
    }
}