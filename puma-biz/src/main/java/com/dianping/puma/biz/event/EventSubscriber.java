package com.dianping.puma.biz.event;

import java.util.List;

public interface EventSubscriber {
    public void setListeners(List<EventListener> listeners);
}
