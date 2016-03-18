package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateUnsupportedException;
import com.dianping.puma.alarm.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.model.result.AlarmResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmArbiterTest {

    PullTimeDelayAlarmArbiter arbiter = new PullTimeDelayAlarmArbiter();

    @Before
    public void setUp() throws Exception {
        arbiter.start();
    }

    @Test
    public void test0() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(true);
        benchmark.setMinPullTimeDelayInSecond(10);
        benchmark.setMaxPullTimeDelayInSecond(10000);

        AlarmResult result = arbiter.arbitrate(data, benchmark);
        assertFalse(result.isAlarm());
    }

    @Test
    public void test1() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(true);
        benchmark.setMinPullTimeDelayInSecond(1000);
        benchmark.setMaxPullTimeDelayInSecond(1000);

        AlarmResult result = arbiter.arbitrate(data, benchmark);
        assertFalse(result.isAlarm());
    }

    @Test
    public void test2() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(true);
        benchmark.setMinPullTimeDelayInSecond(2000);
        benchmark.setMaxPullTimeDelayInSecond(10000);

        AlarmResult result = arbiter.arbitrate(data, benchmark);
        assertTrue(result.isAlarm());
    }

    @Test
    public void test3() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(true);
        benchmark.setMinPullTimeDelayInSecond(10);
        benchmark.setMaxPullTimeDelayInSecond(100);

        AlarmResult result = arbiter.arbitrate(data, benchmark);
        assertTrue(result.isAlarm());
    }

    @Test
    public void test4() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(false);
        benchmark.setMinPullTimeDelayInSecond(10);
        benchmark.setMaxPullTimeDelayInSecond(100);

        AlarmResult result = arbiter.arbitrate(data, benchmark);
        assertFalse(result.isAlarm());
    }

    @Test(expected = PumaAlarmArbitrateUnsupportedException.class)
    public void testException0() throws Exception {
        PushTimeDelayAlarmData data = new PushTimeDelayAlarmData();

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();

        arbiter.arbitrate(data, benchmark);
    }

    @Test(expected = PumaAlarmArbitrateUnsupportedException.class)
    public void testException1() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();

        PushTimeDelayAlarmBenchmark benchmark = new PushTimeDelayAlarmBenchmark();

        arbiter.arbitrate(data, benchmark);
    }

    @After
    public void tearDown() throws Exception {
        arbiter.stop();
    }
}