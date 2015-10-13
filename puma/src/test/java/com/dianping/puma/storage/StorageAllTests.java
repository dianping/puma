package com.dianping.puma.storage;

import com.dianping.puma.storage.data.impl.LocalFileDataBucketManagerTest;
import com.dianping.puma.storage.data.impl.LocalFileReadDataBucketTest;
import com.dianping.puma.storage.data.impl.LocalFileWriteDataBucketTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

		// data.
		LocalFileReadDataBucketTest.class,
		LocalFileDataBucketManagerTest.class,
		LocalFileWriteDataBucketTest.class
})
public class StorageAllTests {
}
