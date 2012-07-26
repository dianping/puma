package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.storage.DefaultArchiveStrategyTest;
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
HDFSBucketIndexTest.class,
HDFSBucketTest.class

})
public class AllTests {

}
