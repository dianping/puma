package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.model.strategy.NoAlarmStrategy;
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
public class ExponentialAlarmRegulatorTest {

    ExponentialAlarmRegulator regulator = new ExponentialAlarmRegulator();

    Clock clock = mock(Clock.class);

    @Before
    public void setUp() throws Exception {
        regulator.setClock(clock);
        regulator.start();
    }

    /**
     * 测试第一次状态异常一定会告警.
     *
     * 指数告警策略: 100,200,200,...
     * 第一次,状态异常,时刻0,告警.
     *
     * @throws Exception
     */
    @Test
    public void test0() throws Exception {
       AlarmResult result = new AlarmResult();

        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(200);

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试告警能否符合指数告警的策略.
     *
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

        AlarmResult result = new AlarmResult();

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(50L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(150L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(300L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试告警在指数告警策略达到最大值是能否正常告警.
     *
     * 指数告警策略:100,200,400,400,...
     * 第一次,状态异常,0时刻,告警.
     * 第二次,状态异常,150时刻,告警.
     * 第三次,状态异常,500时刻,告警.
     * 第四次,状态异常,1000时刻,告警.
     * 第五次,状态异常,1500时刻,告警.
     * 第六次,状态异常,2000时刻,告警.
     *
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(400);

        AlarmResult result = new AlarmResult();

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(150L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1000L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1500L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(2000L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试状态正常能够清空之前的告警状态.
     *
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

        AlarmResult result = new AlarmResult();

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(50L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(60L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试状态正常一直不告警.
     *
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

        AlarmResult result = new AlarmResult();

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(100L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(1000L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());
    }

    /**
     * 测试多个不同名字告警数据能否正常告警.
     *
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

        AlarmResult result = new AlarmResult();

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("a", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = regulator.regulate("b", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(250L);
        result = regulator.regulate("a", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(260L);
        result = regulator.regulate("b", result, strategy);
        assertFalse(result.isAlarm());
    }

    /**
     * 测试非指数告警策略是否会抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmRegulateUnsupportedException.class)
    public void testException0() throws Exception {
        NoAlarmStrategy strategy = new NoAlarmStrategy();
        AlarmResult result = new AlarmResult();
        regulator.regulate("test", result, strategy);
    }

    @After
    public void tearDown() throws Exception {
        regulator.stop();
    }
}