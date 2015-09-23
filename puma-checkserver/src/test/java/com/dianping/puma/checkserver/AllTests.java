package com.dianping.puma.checkserver;

import com.dianping.puma.checkserver.comparison.FullComparisonTest;
import com.dianping.puma.checkserver.fetcher.SingleLineTargetFetcherTest;
import com.dianping.puma.checkserver.fetcher.UpdateTimeAndIdSourceFetcherTest;
import com.dianping.puma.checkserver.mapper.DefaultRowMapperTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        FullComparisonTest.class,
        SingleLineTargetFetcherTest.class,
        UpdateTimeAndIdSourceFetcherTest.class,
        DefaultRowMapperTest.class,
        TaskExecutorTest.class
})
public class AllTests {

}
