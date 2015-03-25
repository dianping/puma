package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaTask;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaTask_")
public class PumaTaskMorphia extends BaseMorphiaEntity<PumaTask> {

	public PumaTaskMorphia() {}

	public PumaTaskMorphia(PumaTask pumaTask) {
		super(pumaTask);
	}
}
