package com.dianping.puma.syncserver.accept;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.accept.exception.SyncAcceptException;

public interface Acceptor extends LifeCycle<SyncAcceptException> {

	void accept(ChangedEvent event) throws SyncAcceptException;

	ChangedEvent take() throws SyncAcceptException;
}
