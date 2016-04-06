package com.dianping.puma.alarm.core.monitor.judge;

import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeUnsupportedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmJudgerTest {

    PullTimeDelayAlarmJudger arbiter = new PullTimeDelayAlarmJudger();

    @Before
    public void setUp() throws Exception {
        arbiter.start();
    }

    /**
     * 测试结果落在范围之内的情况.
     * 结果:1000, 范围:10-10000, 不告警.
     *
     * @throws Exception
     */
    @Test
    public void test0() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(true);
        benchmark.setMinPullTimeDelayInSecond(10);
        benchmark.setMaxPullTimeDelayInSecond(10000);

        AlarmState state = arbiter.judge(data, benchmark);
        assertFalse(state.isAlarm());
    }

    /**
     * 测试结果比范围小的情况.
     * 结果:1000, 范围:2000-10000, 告警.
     *
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(true);
        benchmark.setMinPullTimeDelayInSecond(2000);
        benchmark.setMaxPullTimeDelayInSecond(10000);

        AlarmState state = arbiter.judge(data, benchmark);
        assertTrue(state.isAlarm());
    }

    /**
     * 测试结果比范围大的情况.
     * 结果:1000, 范围:10-100, 告警.
     *
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(true);
        benchmark.setMinPullTimeDelayInSecond(10);
        benchmark.setMaxPullTimeDelayInSecond(100);

        AlarmState state = arbiter.judge(data, benchmark);
        assertTrue(state.isAlarm());
    }

    /**
     * 测试改结果不需要告警的情况.
     * 结果:1000, 范围:10-100, 但是结果不需要告警, 不告警.
     *
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(1000);

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(false);
        benchmark.setMinPullTimeDelayInSecond(10);
        benchmark.setMaxPullTimeDelayInSecond(100);

        AlarmState state = arbiter.judge(data, benchmark);
        assertFalse(state.isAlarm());
    }

    /**
     * 测试非拉取时间数据是否会抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmJudgeUnsupportedException.class)
    public void testException0() throws Exception {
        PushTimeDelayAlarmData data = new PushTimeDelayAlarmData();

        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();

        arbiter.judge(data, benchmark);
    }

    /**
     * 测试非拉取时间范围是否会抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmJudgeUnsupportedException.class)
    public void testException1() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();

        PushTimeDelayAlarmBenchmark benchmark = new PushTimeDelayAlarmBenchmark();

        arbiter.judge(data, benchmark);
    }

    @After
    public void tearDown() throws Exception {
        arbiter.stop();
    }
}