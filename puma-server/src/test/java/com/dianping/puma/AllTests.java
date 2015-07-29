package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.filter.DDLEventFilterTest;
import com.dianping.puma.filter.DMLEventFilterTest;
import com.dianping.puma.filter.DbTbEventFilterTest;
import com.dianping.puma.filter.DefaultEventFilterChainTest;
import com.dianping.puma.filter.DmlDdlEventFilterTest;
import com.dianping.puma.filter.EventFilterChainFactoryTest;
import com.dianping.puma.filter.StorageEventFilterChainTest;
import com.dianping.puma.filter.TransactionEventFilterTest;
import com.dianping.puma.filter.TransactionInfoEventFilterTest;
import com.dianping.puma.pumaserver.channel.impl.DefaultAsyncBinlogChannelTest;
import com.dianping.puma.status.QpsCounterTest;
import com.dianping.puma.storage.DefaultArchiveStrategyTest;
import com.dianping.puma.storage.DefaultBucketManagerTest;
import com.dianping.puma.storage.DefaultCleanupStrategyTest;
import com.dianping.puma.storage.SequenceTest;
import com.dianping.puma.storage.bucket.LocalBucketIndexTest;
import com.dianping.puma.storage.bucket.LocalBucketTest;
import com.dianping.puma.storage.index.DefaultIndexManagerTest;
import com.dianping.puma.storage.index.IndexValueConvertorTest;
import com.dianping.puma.storage.index.LocalFileIndexBucketTest;
import com.dianping.puma.utils.PacketUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
//
      // filter
      DbTbEventFilterTest.class, //
      DDLEventFilterTest.class,//
      DefaultEventFilterChainTest.class,//
      DmlDdlEventFilterTest.class, //
      DMLEventFilterTest.class,//
      EventFilterChainFactoryTest.class,//
      StorageEventFilterChainTest.class,//
      TransactionEventFilterTest.class, //
      TransactionInfoEventFilterTest.class,//

      // status
      QpsCounterTest.class,//

      // storage.bucket
      LocalBucketIndexTest.class, //
      LocalBucketTest.class, //

      // storage.index
      DefaultIndexManagerTest.class,//
      IndexValueConvertorTest.class,//
      LocalFileIndexBucketTest.class,//

      // storage
      DefaultArchiveStrategyTest.class,//
      DefaultBucketManagerTest.class,//
      DefaultCleanupStrategyTest.class,//
      SequenceTest.class,//

      // utils
      PacketUtilTest.class, //
      DefaultAsyncBinlogChannelTest.class })
public class AllTests {

}
