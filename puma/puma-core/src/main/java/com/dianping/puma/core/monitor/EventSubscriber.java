package com.dianping.puma.core.monitor;

import java.util.List;

public interface EventSubscriber {
    public void setListeners(List<EventListener> listeners);
}
