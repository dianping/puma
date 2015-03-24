package com.dianping.puma.core.monitor;

import com.dianping.puma.core.monitor.event.Event;

public interface EventListener {

    void onEvent(Event event);

}
