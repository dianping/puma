package com.dianping.puma.core.monitor;

import com.dianping.puma.core.monitor.event.Event;

public interface EventPublisher {

    void publish(Event event) throws Exception;

}
