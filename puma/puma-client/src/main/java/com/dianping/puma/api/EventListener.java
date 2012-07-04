package com.dianping.puma.api;

import com.dianping.puma.core.event.ChangedEvent;

public interface EventListener {
	public void onEvent(ChangedEvent event);
}
