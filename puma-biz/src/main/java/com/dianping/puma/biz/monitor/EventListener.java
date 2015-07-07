package com.dianping.puma.biz.monitor;

import com.dianping.puma.biz.monitor.event.Event;

public interface EventListener {

    void onEvent(Event event);

}
