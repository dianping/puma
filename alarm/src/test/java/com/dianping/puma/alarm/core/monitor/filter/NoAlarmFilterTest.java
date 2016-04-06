package com.dianping.puma.alarm.core.monitor.filter;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.state.PullTimeDelayAlarmState;
import com.dianping.puma.alarm.core.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.NoAlarmStrategy;
import com.dianping.puma.alarm.exception.PumaAlarmFilterUnsupportedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class NoAlarmFilterTest {

    NoAlarmFilter filter = new NoAlarmFilter();

    @Before
    public void setUp() throws Exception {
        filter.start();
    }

    /**
     * 测试状态异常是否能够不告警.
     *
     * @throws Exception
     */
    @Test
    public void test0() throws Exception {
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        state.setAlarm(true);

        NoAlarmStrategy strategy = new NoAlarmStrategy();

        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());
    }

    /**
     * 测试状态正常是否能够不告警.
     *
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        state.setAlarm(false);

        NoAlarmStrategy strategy = new NoAlarmStrategy();

        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());
    }

    /**
     * 测试非告警策略是否能抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmFilterUnsupportedException.class)
    public void testException0() throws Exception {
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        filter.filter(context, state, strategy);
    }

    @After
    public void tearDown() throws Exception {
        filter.stop();
    }
}