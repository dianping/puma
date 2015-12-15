package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.EventType;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.channel.ReadChannel;
import com.dianping.puma.utils.EventFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

    ReadChannel readChannel;
    ReadChannel eventChannel;
    Queue<Event> eventQueue = new ConcurrentLinkedQueue<Event>();

    DefaultAsyncBinlogChannel target;

    @Before
    public void setUp() throws Exception {
        eventChannel = mock(ReadChannel.class);
        readChannel = mock(ReadChannel.class);

        when(eventChannel.next()).thenAnswer(new Answer<Event>() {
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


        target = spy(new DefaultAsyncBinlogChannel(null));
        doReturn(eventChannel).when(target).initChannel(any(BinlogInfo.class), anyList(), anyBoolean(), anyBoolean(), anyBoolean());
        target.init(new BinlogInfo(-1, "", 1l, 1, 0), "", new ArrayList<String>(), false, false, false);
    }

    @After
    public void tearDown() throws Exception {
        target = null;
        System.gc();
    }

    @Test
    public void testWait() throws Exception {
        Event event1 = EventFactory.ddl(1, 1, "1", 1, "1", "1");
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
    public void testCouldNotBeTooMuch() throws InterruptedException {
        for (int k = 0; k < 2000; k++) {
            eventQueue.add(EventFactory.ddl(1, 1, "1", 1, "1", "1"));
        }
        Thread.sleep(100);
        Assert.assertTrue(eventQueue.size() >= 500 && eventQueue.size() < 1500);
    }

    @Test
    public void testBatch() throws Exception {
        Event event1 = EventFactory.ddl(1, 1, "1", 1, "1", "1");
        Event event2 = EventFactory.ddl(1, 1, "1", 1, "1", "1");
        Event event3 = EventFactory.ddl(1, 1, "1", 1, "1", "1");
        Event event4 = EventFactory.ddl(1, 1, "1", 1, "1", "1");

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