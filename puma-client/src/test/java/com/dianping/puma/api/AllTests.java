package com.dianping.puma.api;

import com.dianping.puma.api.impl.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ClusterPumaClientTest.class,
        ConfigPumaServerMonitorTest.class,
        LogTest.class,
        RoundRobinPumaServerRouterTest.class,
        SimplePumaClientTest.class
})
public class AllTests {

}
