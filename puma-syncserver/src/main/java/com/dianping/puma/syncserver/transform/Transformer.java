package com.dianping.puma.syncserver.transform;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;

public interface Transformer extends LifeCycle<Exception> {

	ChangedEvent transform(ChangedEvent binlogEvent);
}
