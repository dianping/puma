package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.old.CatchupTask;
import com.google.code.morphia.annotations.Entity;

@Entity("CatchupTask_")
public class CatchupTaskMorphia extends BaseMorphiaEntity<CatchupTask> {

	public CatchupTaskMorphia() {}

	public CatchupTaskMorphia(CatchupTask catchupTask) { super(catchupTask); }
}
