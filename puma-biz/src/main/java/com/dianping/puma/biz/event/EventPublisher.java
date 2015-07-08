package com.dianping.puma.biz.event;

import com.dianping.puma.biz.event.entity.Event;

public interface EventPublisher {

    void publish(Event event) throws Exception;

}
