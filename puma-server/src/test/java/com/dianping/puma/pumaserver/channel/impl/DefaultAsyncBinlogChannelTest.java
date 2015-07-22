package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.EventType;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Dozer @ 7/17/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultAsyncBinlogChannelTest {


    TaskContainer taskContainer;
    EventStorage eventStorage;
    EventChannel eventChannel;
    Queue<Event> eventQueue = new ConcurrentLinkedQueue<Event>();

    DefaultAsyncBinlogChannel target;

    @Before
    public void setUp() throws Exception {
        eventChannel = mock(EventChannel.class);
        eventStorage = mock(EventStorage.class);
        taskContainer = mock(TaskContainer.class);

        when(taskContainer.getTaskStorage(anyString())).thenReturn(eventStorage);
        when(eventStorage.getChannel(anyLong(), anyLong(), anyString(), anyLong(), anyLong())).thenReturn(eventChannel);
        when(eventChannel.next(anyBoolean())).thenAnswer(new Answer<Event>() {
            @Override
            public Event answer(InvocationOnMock invocationOnMock) throws Throwable {
                Event event = eventQueue.poll();
                if (event instanceof NullEvent) {
                    return null;
                } else {
                    return event;
                }
            }
        });


        target = new DefaultAsyncBinlogChannel();
        target.setTaskContainer(taskContainer);
        target.init(-1, new BinlogInfo(-1, "", 1l, 1), -1, "", new ArrayList<String>(), false, false, false);
    }

    @Test
    public void testWait() throws Exception {
        Event event1 = new ServerErrorEvent("1");
        final AtomicReference<BinlogGetResponse> result = new AtomicReference<BinlogGetResponse>();
        Channel channel = getChannel(result);

        BinlogGetRequest request = new BinlogGetRequest().setChannel(channel);
        target.addRequest(request);

        assertNull(result.get());

        eventQueue.offer(event1);

        Thread.sleep(20);

        BinlogGetResponse response = result.get();
        assertNotNull(response);
        assertEquals(1, response.getBinlogMessage().getBinlogEvents().size());
        assertSame(event1, response.getBinlogMessage().getBinlogEvents().get(0));
    }

    @Test
    public void testBatch() throws Exception {
        Event event1 = new ServerErrorEvent("1");
        Event event2 = new ServerErrorEvent("2");
        Event event3 = new ServerErrorEvent("3");
        Event event4 = new ServerErrorEvent("4");

        eventQueue.add(event1);
        eventQueue.add(event2);
        eventQueue.add(event3);
        eventQueue.add(event4);

        final AtomicReference<BinlogGetResponse> result = new AtomicReference<BinlogGetResponse>();
        Channel channel = getChannel(result);

        BinlogGetRequest request = new BinlogGetRequest()
                .setBatchSize(2)
                .setChannel(channel);

        target.addRequest(request);

        Thread.sleep(100);

        BinlogGetResponse response = result.get();
        assertNotNull(response);
        assertEquals(2, response.getBinlogMessage().getBinlogEvents().size());
        assertSame(event1, response.getBinlogMessage().getBinlogEvents().get(0));
        assertSame(event2, response.getBinlogMessage().getBinlogEvents().get(1));
    }

    @Test
    public void testTimeout() throws Exception {
        final AtomicReference<BinlogGetResponse> result = new AtomicReference<BinlogGetResponse>();
        Channel channel = getChannel(result);

        BinlogGetRequest request = new BinlogGetRequest()
                .setStartTime(System.currentTimeMillis())
                .setTimeout(100)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setChannel(channel);

        target.addRequest(request);

        Thread.sleep(150);

        assertNotNull(result.get());
    }

    @Test
    public void test_weakreference() throws Exception {
        Assert.assertEquals(1, ((ThreadPoolExecutor) DefaultAsyncBinlogChannel.executorService).getActiveCount());
        target = null;
        System.gc();
        Thread.sleep(50);
        Assert.assertEquals(0, ((ThreadPoolExecutor) DefaultAsyncBinlogChannel.executorService).getActiveCount());
    }

    protected Channel getChannel(final AtomicReference<BinlogGetResponse> result) {
        Channel channel = mock(Channel.class);
        when(channel.isActive()).thenReturn(true);
        when(channel.writeAndFlush(anyObject())).thenAnswer(new Answer<ChannelFuture>() {
            @Override
            public ChannelFuture answer(InvocationOnMock invocationOnMock) throws Throwable {
                result.set((BinlogGetResponse) invocationOnMock.getArguments()[0]);
                return null;
            }
        });
        return channel;
    }

    @SuppressWarnings("serial")
    static class NullEvent extends Event {
        @Override
        public BinlogInfo getBinlogInfo() {
            return null;
        }

        @Override
        public EventType getEventType() {
            return null;
        }
    }
}