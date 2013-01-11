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
import com.dianping.puma.integration.DataTypeIntegrationTest;
import com.dianping.puma.integration.IndexIntegegrationTest;
import com.dianping.puma.integration.SQLIntegegrationTest;
import com.dianping.puma.storage.DefaultArchiveStrategyTest;
import com.dianping.puma.storage.DefaultBucketManagerTest;
import com.dianping.puma.storage.DefaultCleanupStrategyTest;
import com.dianping.puma.storage.DefaultDataIndexTest;
import com.dianping.puma.storage.HDFSBucketIndexTest;
import com.dianping.puma.storage.HDFSBucketTest;
import com.dianping.puma.storage.LocalBucketIndexTest;
import com.dianping.puma.storage.LocalBucketTest;
import com.dianping.puma.storage.SequenceTest;

@RunWith(Suite.class)
@SuiteClasses( {

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
SQLIntegegrationTest.class,
//SystemIntegegrationTest.class,
DataTypeIntegrationTest.class,
PacketUtilTest.class,
DefaultCleanupStrategyTest.class,
DefaultDataIndexTest.class,
IndexIntegegrationTest.class
})
public class AllTests {

}
