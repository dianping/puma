package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.model.strategy.NoAlarmStrategy;
import com.dianping.puma.common.utils.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class LinearAlarmRegulatorTest {

    LinearAlarmRegulator regulator = new LinearAlarmRegulator();

    Clock clock = mock(Clock.class);

    @Before
    public void setUp() throws Exception {
        regulator.setClock(clock);
        regulator.start();
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
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("test", result, strategy);
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
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

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
        when(clock.getTimestamp()).thenReturn(200L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(300L);
        result = regulator.regulate("test", result, strategy);
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
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

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
     * 线性告警策略: 100,100,...
     * 第一次,状态正常,时刻0,不告警.
     * 第二次,状态正常,时刻50,不告警.
     *
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(50L);
        result = regulator.regulate("test", result, strategy);
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
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(0L);
        result = regulator.regulate("a", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(50L);
        result = regulator.regulate("b", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(120L);
        result = regulator.regulate("a", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = regulator.regulate("b", result, strategy);
        assertTrue(result.isAlarm());
    }

    /**
     * 测试非线性策略会抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmRegulateUnsupportedException.class)
    public void testException0() throws Exception {
        AlarmResult result = new AlarmResult();

        NoAlarmStrategy strategy = new NoAlarmStrategy();

        regulator.regulate("test", result, strategy);
    }

    @After
    public void tearDown() throws Exception {
        regulator.stop();
    }
}