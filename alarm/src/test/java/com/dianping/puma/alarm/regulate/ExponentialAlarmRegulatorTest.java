package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.common.utils.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void test0() throws Exception {
        AlarmResult result = new AlarmResult();

        ExponentialAlarmStrategy strategy = new ExponentialAlarmStrategy();
        strategy.setMinExponentialAlarmIntervalInSecond(100);
        strategy.setMaxExponentialAlarmIntervalInSecond(300);

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(100L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(150L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(210L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(350L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(500L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(810L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1120L);
        result = regulator.regulate("test", result, strategy);
        assertTrue(result.isAlarm());

        result.setAlarm(true);
        when(clock.getTimestamp()).thenReturn(1400L);
        result = regulator.regulate("test", result, strategy);
        assertFalse(result.isAlarm());
    }

    @After
    public void tearDown() throws Exception {
        regulator.stop();
    }
}