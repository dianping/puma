package com.dianping.puma.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.core.util.ByteArrayUtilsTest;
import com.dianping.puma.core.util.StreamUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({

// add test classes here
ByteArrayUtilsTest.class,
StreamUtilsTest.class

})
public class AllTests {

}
