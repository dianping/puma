package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.common.util.PacketUtilTest;
import com.dianping.puma.filter.DbTbEventFilterTest;
import com.dianping.puma.filter.DefaultEventFilterChainTest;
import com.dianping.puma.filter.DmlDdlEventFilterTest;
import com.dianping.puma.filter.EventFilterChainFactoryTest;
import com.dianping.puma.filter.TransactionInfoEventFilterTest;
import com.dianping.puma.storage.DefaultDataIndexTest;
import com.dianping.puma.storage.LocalBucketTest;
import com.dianping.puma.storage.SequenceTest;


@RunWith(Suite.class)
@SuiteClasses({
	// add test classes here
        SequenceTest.class,
        LocalBucketTest.class,
        //LocalBucketIndexTest.class,
        //DefaultArchiveStrategyTest.class,
        //DefaultBucketManagerTest.class,
		//HDFSBucketIndexTest.class,
		//HDFSBucketTest.class,
        DbTbEventFilterTest.class,
        DefaultEventFilterChainTest.class,
        DmlDdlEventFilterTest.class,
        EventFilterChainFactoryTest.class,
        TransactionInfoEventFilterTest.class,
        //SQLIntegegrationTest.class,
        //SystemIntegegrationTest.class,
        //DataTypeIntegrationTest.class,
        PacketUtilTest.class,
        //DefaultCleanupStrategyTest.class,
        DefaultDataIndexTest.class
        //IndexIntegrationTest.class
})
public class AllTests {

}
