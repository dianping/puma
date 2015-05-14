package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.load.exception.LoadException;

public interface Loader {

	void load(ChangedEvent event) throws LoadException;

	void start();

	void stop();
}
