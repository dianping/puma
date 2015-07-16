package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.filter.DbTbEventFilterTest;
import com.dianping.puma.filter.DefaultEventFilterChainTest;
import com.dianping.puma.filter.DmlDdlEventFilterTest;
import com.dianping.puma.filter.EventFilterChainFactoryTest;
import com.dianping.puma.filter.StorageEventFilterChainTest;
import com.dianping.puma.filter.TransactionEventFilterTest;
import com.dianping.puma.filter.TransactionInfoEventFilterTest;
import com.dianping.puma.storage.SequenceTest;
import com.dianping.puma.storage.bucket.LocalBucketTest;
import com.dianping.puma.storage.index.DefaultDataIndexTest;
import com.dianping.puma.utils.PacketUtilTest;


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
        StorageEventFilterChainTest.class,
        TransactionEventFilterTest.class,
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
