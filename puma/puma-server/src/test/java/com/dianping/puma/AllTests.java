package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.filter.DbTbEventFilterTest;
import com.dianping.puma.filter.DefaultEventFilterChainTest;
import com.dianping.puma.filter.DmlDdlEventFilterTest;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.filter.TransactionInfoEventFilterTest;
import com.dianping.puma.integration.SQLIntegegrationTest;
import com.dianping.puma.storage.DefaultArchiveStrategyTest;
import com.dianping.puma.storage.DefaultBucketManagerTest;
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
HDFSBucketIndexTest.class,
HDFSBucketTest.class,
DbTbEventFilterTest.class,
DefaultEventFilterChainTest.class,
DmlDdlEventFilterTest.class,
EventFilterChainFactory.class,
TransactionInfoEventFilterTest.class,
SQLIntegegrationTest.class

})
public class AllTests {

}
