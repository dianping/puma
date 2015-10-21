package com.dianping.puma.storage;

import com.dianping.puma.storage.bucket.LengthReadBucketTest;
import com.dianping.puma.storage.bucket.LengthWriteBucketTest;
import com.dianping.puma.storage.bucket.LineReadBucketTest;
import com.dianping.puma.storage.bucket.LineWriteBucketTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		LineReadBucketTest.class,
		LineWriteBucketTest.class,
		LengthReadBucketTest.class,
		LengthWriteBucketTest.class
})
public class StorageAllTest {
}
