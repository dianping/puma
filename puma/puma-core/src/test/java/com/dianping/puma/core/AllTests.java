package com.dianping.puma.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.core.codec.EventCodecFactoryTest;
import com.dianping.puma.core.codec.JsonEventCodecTest;
import com.dianping.puma.core.util.ByteArrayUtilsTest;
import com.dianping.puma.core.util.StreamUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({

// add test classes here
JsonEventCodecTest.class,
EventCodecFactoryTest.class,
ByteArrayUtilsTest.class,
StreamUtilsTest.class

})
public class AllTests {

}
