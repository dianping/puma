package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.DumpTask;
import com.google.code.morphia.annotations.Entity;

@Entity("DumpTask_")
public class DumpTaskMorphia extends BaseMorphiaEntity<DumpTask> {

	public DumpTaskMorphia() {}

	public DumpTaskMorphia(DumpTask dumpTask) { super(dumpTask); }
}
