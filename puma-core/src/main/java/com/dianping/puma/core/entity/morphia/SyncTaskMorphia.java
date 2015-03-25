package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.SyncTask;
import com.google.code.morphia.annotations.Entity;

@Entity("SyncTask_")
public class SyncTaskMorphia extends BaseMorphiaEntity<SyncTask> {

	public SyncTaskMorphia() {}

	public SyncTaskMorphia(SyncTask syncTask) { super(syncTask); }
}
