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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void test0() throws Exception {
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(290L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(310L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(550L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());
    }

    @Test
    public void test1() throws Exception {
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(200L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(290L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(310L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(false);
        when(clock.getTimestamp()).thenReturn(550L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());
    }

    @Test
    public void test3() throws Exception {
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();
        strategy.setLinearAlarmIntervalInSecond(100);

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(200L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(290L);
        result = regulator.regulate("abcd", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(310L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = regulator.regulate("abcd", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(550L);
        result = regulator.regulate("text", result, strategy);
        assertTrue(result.isAlarm());
    }

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