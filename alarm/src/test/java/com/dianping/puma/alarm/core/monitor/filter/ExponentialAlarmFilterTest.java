package com.dianping.puma.alarm.core.monitor.filter;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.state.PullTimeDelayAlarmState;
import com.dianping.puma.alarm.core.model.state.PushTimeDelayAlarmState;
import com.dianping.puma.alarm.core.model.strategy.ExponentialAlarmStrategy;
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
 * Created by xiaotian.li on 16/3/23.
 * Email: lixiaotian07@gmail.com
 */
public class ExponentialAlarmFilterTest {

    ExponentialAlarmFilter filter = new ExponentialAlarmFilter();

    Clock clock = mock(Clock.class);

    @Before
    public void setUp() throws Exception {
        filter.setClock(clock);
        filter.start();
    }

    /**
     * 测试第一次状态异常一定会告警.
     * <p/>
     * 指数告警策略: 100,200,200,...
     * 第一次,状态异常,时刻0,告警.
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

        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(200);

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试告警能否符合指数告警的策略.
     * <p/>
     * 指数告警策略: 100,200,400,800,...
     * 第一次,状态异常,0时刻,告警.
     * 第二次,状态异常,50时刻,不告警.
     * 第三次,状态异常,150时刻,告警.
     * 第四次,状态异常,300时刻,不告警.
     * 第五次,状态异常,500时刻,告警.
     *
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(10000);

        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

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
        when(clock.getTimestamp()).thenReturn(300L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试告警在指数告警策略达到最大值是能否正常告警.
     * <p/>
     * 指数告警策略:100,200,400,400,...
     * 第1次,状态异常,0时刻,告警.
     * 第2次,状态异常,150时刻,告警.
     * 第3次,状态异常,500时刻,告警.
     * 第4次,状态异常,1000时刻,告警.
     * 第5次,状态异常,1500时刻,告警.
     * 第6次,状态异常,1501时刻,不告警.
     * 第7次,状态异常,1502时刻,不告警.
     * 第8次,状态异常,1503时刻,不告警.
     * 第9次,状态异常,2000时刻,告警.
     *
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(400);

        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(150L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1000L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1500L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1501L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1502L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1503L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(2000L);
        result = filter.filter(context, state, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试状态正常能够清空之前的告警状态.
     * <p/>
     * 指数告警策略:100,200,200...
     * 第一次,状态异常,0时刻,告警.
     * 第二次,状态正常,50时刻,不告警.
     * 第三次,状态异常,60时刻,告警.
     *
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(200);

        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

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
     * <p/>
     * 指数告警策略:100,200,200,...
     * 第一次,状态正常,时刻0,不告警.
     * 第二次,状态正常,时刻100,不告警.
     * 第三次,状态正常,时刻1000,不告警.
     *
     * @throws Exception
     */
    @Test
    public void test4() throws Exception {
        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(200);

        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        state.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(100L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());

        state.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(1000L);
        result = filter.filter(context, state, strategy);
        assertFalse(result.isAlarm());
    }

    /**
     * 测试多个不同名字告警数据能否正常告警.
     * <p/>
     * 指数告警策略: 0,100,200,200,...
     * 第一次,名字a,时刻0,告警.
     * 第二次,名字b,时刻200,告警.
     * 第三次,名字a,时刻250,告警.
     * 第四次,名字b,时刻280,不告警.
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(200);

        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        AlarmResult result;

        AlarmContext context0 = new AlarmContext();
        context0.setNamespace("client");
        context0.setName("a");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = filter.filter(context0, state, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context1 = new AlarmContext();
        context1.setNamespace("client");
        context1.setName("b");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = filter.filter(context1, state, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context2 = new AlarmContext();
        context2.setNamespace("client");
        context2.setName("a");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(250L);
        result = filter.filter(context2, state, strategy);
        assertTrue(result.isAlarm());

        AlarmContext context3 = new AlarmContext();
        context3.setNamespace("client");
        context3.setName("b");

        state.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(260L);
        result = filter.filter(context3, state, strategy);
        assertFalse(result.isAlarm());
    }

    /**
     * 测试同名不同类型告警数据能否正常告警.
     * <p/>
     * 指数告警策略: 0,100,200,200,...
     * 第一次,类型a,状态异常,时刻0,告警.
     * 第二次,类型b,状态正常,时刻50,不告警.
     * 第三次,类型a,状态异常,时刻120,告警.
     * 第四次,类型b,状态异常,时刻200,告警.
     *
     * @throws Exception
     */
    @Test
    public void test6() throws Exception {
        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(200);

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
        context1.setNamespace("client");
        context1.setName("test");
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
     * 测试非指数告警策略是否会抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmFilterUnsupportedException.class)
    public void testException0() throws Exception {
        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");

        NoAlarmStrategy strategy = new NoAlarmStrategy();
        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();
        filter.filter(context, state, strategy);
    }

    @After
    public void tearDown() throws Exception {
        filter.stop();
    }
}