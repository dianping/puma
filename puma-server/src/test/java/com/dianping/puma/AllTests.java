package com.dianping.puma;

import com.dianping.puma.filter.*;
import com.dianping.puma.pumaserver.channel.impl.DefaultAsyncBinlogChannelTest;
import com.dianping.puma.status.QpsCounterTest;
import com.dianping.puma.storage.DefaultArchiveStrategyTest;
import com.dianping.puma.storage.DefaultBucketManagerTest;
import com.dianping.puma.storage.DefaultCleanupStrategyTest;
import com.dianping.puma.storage.SequenceTest;
import com.dianping.puma.storage.bucket.LocalBucketIndexTest;
import com.dianping.puma.storage.bucket.LocalBucketTest;
import com.dianping.puma.storage.index.DefaultDataIndexTest;
import com.dianping.puma.storage.index.L2IndexItemConvertorTest;
import com.dianping.puma.storage.index.LocalFileIndexBucketTest;
import com.dianping.puma.utils.PacketUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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

        //status
        QpsCounterTest.class,//

        // storage.bucket
        LocalBucketIndexTest.class, //
        LocalBucketTest.class, //

        // storage.index
        DefaultDataIndexTest.class,//
        L2IndexItemConvertorTest.class,//
        LocalFileIndexBucketTest.class,//

        // storage
        DefaultArchiveStrategyTest.class,//
        DefaultBucketManagerTest.class,//
        DefaultCleanupStrategyTest.class,//
        SequenceTest.class,//

        // utils
        PacketUtilTest.class, //
        DefaultDataIndexTest.class, //
        DefaultAsyncBinlogChannelTest.class
})
public class AllTests {

}
