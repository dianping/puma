package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.old.SyncTask;
import com.google.code.morphia.annotations.Entity;

@Entity("SyncTask_")
public class SyncTaskMorphia extends BaseMorphiaEntity<SyncTask> {

	public SyncTaskMorphia() {}

	public SyncTaskMorphia(SyncTask syncTask) { super(syncTask); }
}
