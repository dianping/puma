package com.dianping.puma.biz.monitor;

import com.dianping.puma.biz.monitor.event.Event;

public interface EventPublisher {

    void publish(Event event) throws Exception;

}
