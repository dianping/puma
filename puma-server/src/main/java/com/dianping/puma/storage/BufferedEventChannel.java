/**
 * Project: puma-server
 * 
 * File Created at Nov 29, 2013
 * 
 */
package com.dianping.puma.storage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.exception.StorageException;

/**
 * @author Leo Liang
 * 
 */
public class BufferedEventChannel implements EventChannel {
    private static final AtomicLong     seq     = new AtomicLong(0);

    private EventChannel                eventChannel;
    private BlockingQueue<Event> eventBuffer;
    private volatile boolean            stopped = false;

    public BufferedEventChannel(EventChannel eventChannel, int bufSize) {
        this.eventChannel = eventChannel;
        this.eventBuffer = new ArrayBlockingQueue<Event>(bufSize);

        Thread fillThread = new Thread(new Runnable() {

            @Override
            public void run() {
                extract();
            }

        });
        fillThread.setDaemon(true);
        fillThread.setName("BufferedChannelExtractThread-" + seq.incrementAndGet());
        fillThread.start();
    }

    @Override
    public void close() {
        stopped = true;
        eventChannel.close();
    }

    @Override
    public void start() {
        stopped = false;
        eventChannel.start();
    }

    private void extract() {
        while (!stopped) {
            try {
                eventBuffer.put(eventChannel.next());
            } catch (Throwable e) {
            }
        }
    }

    @Override
    public Event next() throws StorageException {
        try {
            return eventBuffer.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

}
