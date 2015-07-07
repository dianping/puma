package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.PumaTask;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaTask_")
public class PumaTaskMorphia extends BaseMorphiaEntity<PumaTask> {

	public PumaTaskMorphia() {}

	public PumaTaskMorphia(PumaTask pumaTask) {
		super(pumaTask);
	}
}
