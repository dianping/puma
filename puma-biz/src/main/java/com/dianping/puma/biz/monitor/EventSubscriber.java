package com.dianping.puma.biz.monitor;

import java.util.List;

public interface EventSubscriber {
    public void setListeners(List<EventListener> listeners);
}
