package com.dianping.puma.core;

import com.dianping.puma.core.dto.binlog.request.BinlogGetRequestTest;
import com.dianping.puma.core.util.ByteArrayUtilsTest;
import com.dianping.puma.core.util.StreamUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

// add test classes here
        BinlogGetRequestTest.class,
        ByteArrayUtilsTest.class,
        StreamUtilsTest.class

})
public class AllTests {

}
