package com.dianping.puma.storage;

import com.dianping.puma.storage.bucket.LineReadBucketTest;
import com.dianping.puma.storage.bucket.LineWriteBucketTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		LineReadBucketTest.class,
		LineWriteBucketTest.class
})
public class StorageAllTest {
}
