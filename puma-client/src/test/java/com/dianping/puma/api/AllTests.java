package com.dianping.puma.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( {

// add test classes here
ConfigurationBuilderTest.class,
ConfigurationTest.class,
PumaClientTest.class

})
public class AllTests {

}
