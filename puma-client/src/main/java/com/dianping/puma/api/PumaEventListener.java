package com.dianping.puma.api;

import com.dianping.puma.core.event.ChangedEvent;

public interface PumaEventListener {

	public void onEvent(ChangedEvent changedEvent);
}
