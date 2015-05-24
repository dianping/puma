package com.dianping.puma.syncserver.job.transform;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.LifeCycle;
import com.dianping.puma.syncserver.job.transform.exception.TransformException;

public interface Transformer extends LifeCycle {

	public void transform(ChangedEvent event) throws TransformException;
}
