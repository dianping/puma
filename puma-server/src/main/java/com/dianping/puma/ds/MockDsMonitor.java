package com.dianping.puma.ds;

import org.springframework.stereotype.Service;

/**
 * Dozer @ 8/11/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class MockDsMonitor implements DsMonitor {
    @Override
    public Cluster getCluster(String clusterName) {
        Cluster cluster = new Cluster();
        cluster.addDatabase("test");

        Single s1 = new Single();
        s1.setHost("192.168.225.83");
        s1.setPort(3306);
        s1.setActive(true);
        s1.setBalance(1);
        s1.setMaster(true);

        Single s2 = new Single();
        s2.setHost("192.168.225.84");
        s2.setPort(3306);
        s2.setActive(true);
        s2.setBalance(1);

        Single s3 = new Single();
        s3.setHost("192.168.225.85");
        s3.setPort(3306);
        s3.setActive(true);
        s3.setBalance(1);

        cluster.addSingle(s1);
        cluster.addSingle(s2);
        cluster.addSingle(s3);
        return cluster;
    }

    @Override
    public void addListener(String clusterName, DsMonitorListener listener) {

    }

    @Override
    public void removeListener(String clusterName) {

    }
}
