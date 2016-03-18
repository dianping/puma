package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.model.strategy.NoAlarmStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class NoAlarmRegulatorTest {

    NoAlarmRegulator regulator = new NoAlarmRegulator();

    @Before
    public void setUp() throws Exception {
        regulator.start();
    }

    @Test
    public void test0() throws Exception {
        AlarmResult result = new AlarmResult();
        result.setAlarm(true);

        NoAlarmStrategy strategy = new NoAlarmStrategy();

        result = regulator.regulate("abc", result, strategy);
        assertFalse(result.isAlarm());
    }

    @Test
    public void test1() throws Exception {
        AlarmResult result = new AlarmResult();
        result.setAlarm(false);

        NoAlarmStrategy strategy = new NoAlarmStrategy();

        result = regulator.regulate("abc", result, strategy);
        assertFalse(result.isAlarm());
    }

    @Test(expected = PumaAlarmRegulateUnsupportedException.class)
    public void testException0() throws Exception {
        AlarmResult result = new AlarmResult();

        LinearAlarmStrategy strategy = new LinearAlarmStrategy();

        regulator.regulate("abc", result, strategy);
    }

    @After
    public void tearDown() throws Exception {
        regulator.stop();
    }
}