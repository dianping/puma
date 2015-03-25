package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.CatchupTask;
import com.google.code.morphia.annotations.Entity;

@Entity("CatchupTask_")
public class CatchupTaskMorphia extends BaseMorphiaEntity<CatchupTask> {

	public CatchupTaskMorphia() {}

	public CatchupTaskMorphia(CatchupTask catchupTask) { super(catchupTask); }
}
