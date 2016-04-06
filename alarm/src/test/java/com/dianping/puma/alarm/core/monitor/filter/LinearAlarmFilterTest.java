package com.dianping.puma.alarm.core.monitor.filter;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.state.PullTimeDelayAlarmState;
import com.dianping.puma.alarm.core.model.state.PushTimeDelayAlarmState;
import com.dianping.puma.alarm.core.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.NoAlarmStrategy;
import com.dianping.puma.alarm.exception.PumaAlarmFilterUnsupportedException;
import com.dianping.puma.common.utils.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class LinearAlarmFilterTest {

    LinearAlarmFilter filter = new LinearAlarmFilter();

    Clock clock = mock(Clock.class);

    @Before
    public void setUp() throws Exception {
        filter.setClock(clock);
        filter.start();
    }

    /**
     * 测试第一次状态异常一定能够告警.
     *
     * 线性告警策略: 100,100,...
     * 第一次,状态异常,时刻0, 告警
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

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试多次告警需要符合线性告警策略.
     *
     * 线性告警策略: 100,100,...
     * 第一次,状态异常,时刻0,告警.
     * 第二次,状态异常,时刻50,不告警.
     * 第三次,状态异常,时刻150,告警.
     * 第四次,状态异常,时刻200,不告警.
     * 第五次,状态异常,时刻300,告警.
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

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(50L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(150L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(300L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试正常状态可以清楚告警状态.
     *
     * 线性告警策略: 100,100,...
     * 第一次,状态异常,时刻0,告警.
     * 第二次,状态正常,时刻50,不告警.
     * 第三次,状态异常,时刻60,告警.
     *
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(50L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(60L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试状态正常一直不告警.
     *
     * 线性告警策略: 100,100,...
     * 第一次,状态正常,时刻0,不告警.
     * 第二次,状态正常,时刻50,不告警.
     *
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        state.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(50L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());
    }

    /**
     * 测试多个名字告警.
     *
     * 线性告警策略: 100,100,...
     * 第一次,名字a,状态异常,时刻0,告警.
     * 第二次,名字b,状态异常,时刻50,告警.
     * 第三次,名字a,状态异常,时刻120,告警.
     * 第四次,名字b,状态异常,时刻200,告警.
     *
     * @throws Exception
     */
    @Test
    public void test4() throws Exception {
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        AlarmContext context0 = new AlarmContext();
        context0.setNamespace("client");
        context0.setName("a");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context0, state, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context1 = new AlarmContext();
        context0.setNamespace("client");
        context0.setName("b");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(50L);
        result = filter.filter(context1, state, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context2 = new AlarmContext();
        context2.setNamespace("client");
        context2.setName("a");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(120L);
        result = filter.filter(context2, state, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context3 = new AlarmContext();
        context3.setNamespace("client");
        context3.setName("b");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = filter.filter(context3, state, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试同名多种类型告警.
     *
     * 线性告警策略: 100,100,...
     * 第一次,类型a,状态异常,时刻0,告警.
     * 第二次,类型b,状态正常,时刻50,不告警告警.
     * 第三次,类型a,状态异常,时刻120,告警.
     * 第四次,类型b,状态异常,时刻200,告警.
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        AlarmResult result;

        AlarmContext context0 = new AlarmContext();
        context0.setNamespace("client");
        context0.setName("test");
        PullTimeDelayAlarmState state0 = new PullTimeDelayAlarmState();
        state0.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context0, state0, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context1 = new AlarmContext();
        context0.setNamespace("client");
        context0.setName("test");
        PushTimeDelayAlarmState state1 = new PushTimeDelayAlarmState();
        state1.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(50L);
        result = filter.filter(context1, state1, strategy);
        assertFalse(result.isAlarm());

        AlarmContext context2 = new AlarmContext();
        context2.setNamespace("client");
        context2.setName("test");
        PullTimeDelayAlarmState state2 = new PullTimeDelayAlarmState();
        state2.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(120L);
        result = filter.filter(context2, state2, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context3 = new AlarmContext();
        context3.setNamespace("client");
        context3.setName("test");
        PushTimeDelayAlarmState state3 = new PushTimeDelayAlarmState();
        state3.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = filter.filter(context3, state3, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试非线性策略会抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmFilterUnsupportedException.class)
    public void testException0() throws Exception {
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();

        NoAlarmStrategy strategy = new NoAlarmStrategy();

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