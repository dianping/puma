package com.dianping.puma.syncserver.transform;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.transform.exception.TransformException;

public interface Transformer extends LifeCycle<TransformException> {

	ChangedEvent transform(ChangedEvent event) throws TransformException;
}
