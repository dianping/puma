package com.dianping.puma.alarm.core.monitor.render;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmMessage;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.exception.PumaAlarmRenderUnsupportedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmRendererTest {

    PushTimeDelayAlarmRenderer renderer = new PushTimeDelayAlarmRenderer();

    @Before
    public void setUp() throws Exception {
        renderer.setTitleTemplate("%s");
        renderer.setContentTemplate("%s%s%s");
        renderer.start();
    }

    /**
     * 测试正常的标题和内容替换功能.
     *
     * @throws Exception
     */
    @Test
    public void test0() throws Exception {
        PushTimeDelayAlarmData data = new PushTimeDelayAlarmData();
        data.setPushTimeDelayInSecond(100);

        PushTimeDelayAlarmBenchmark benchmark = new PushTimeDelayAlarmBenchmark();
        benchmark.setPushTimeDelayAlarm(true);
        benchmark.setMinPushTimeDelayInSecond(1000);
        benchmark.setMaxPushTimeDelayInSecond(10000);

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");
        AlarmMessage message = renderer.render(context, data, benchmark);
        assertEquals(context.getName(), message.getTitle());
        assertEquals("100100010000", message.getContent());
    }

    /**
     * 测试非拉取时间类型会抛出异常.
     *
     * @throws Exception
     */
    @Test(expected = PumaAlarmRenderUnsupportedException.class)
    public void testException0() throws Exception {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(100);

        PushTimeDelayAlarmBenchmark benchmark = new PushTimeDelayAlarmBenchmark();
        benchmark.setPushTimeDelayAlarm(true);
        benchmark.setMinPushTimeDelayInSecond(1000);
        benchmark.setMaxPushTimeDelayInSecond(10000);

        AlarmContext context = new AlarmContext();
        context.setNamespace("client");
        context.setName("test");
        renderer.render(context, data, benchmark);
    }

    @After
    public void tearDown() throws Exception {
        renderer.stop();
    }

}