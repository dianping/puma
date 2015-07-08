package com.dianping.puma.biz.event;

import com.dianping.puma.biz.event.entity.Event;

public interface EventListener {

    void onEvent(Event event);

}
