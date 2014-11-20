package com.dianping.puma.core.monitor;

public interface EventPublisher {

    void publish(Event event) throws Exception;

}
