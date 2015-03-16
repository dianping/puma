package com.dianping.puma;

import com.dianping.puma.common.util.PacketUtilTest;
import com.dianping.puma.filter.*;
import com.dianping.puma.storage.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

// add test classes here
        SequenceTest.class,
        LocalBucketTest.class,
        LocalBucketIndexTest.class,
        DefaultArchiveStrategyTest.class,
        DefaultBucketManagerTest.class,
//HDFSBucketIndexTest.class,
//HDFSBucketTest.class,
        DbTbEventFilterTest.class,
        DefaultEventFilterChainTest.class,
        DmlDdlEventFilterTest.class,
        EventFilterChainFactoryTest.class,
        TransactionInfoEventFilterTest.class,
//        SQLIntegegrationTest.class,
//SystemIntegegrationTest.class,
//DataTypeIntegrationTest.class,
        PacketUtilTest.class,
        DefaultCleanupStrategyTest.class,
        DefaultDataIndexTest.class,
//        IndexIntegegrationTest.class
})
public class AllTests {

}
