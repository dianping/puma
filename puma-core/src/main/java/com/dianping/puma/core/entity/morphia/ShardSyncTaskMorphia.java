package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.ShardSyncTask;
import com.dianping.puma.core.entity.SyncTask;
import com.google.code.morphia.annotations.Entity;

@Entity("ShardSyncTask")
public class ShardSyncTaskMorphia extends BaseMorphiaEntity<ShardSyncTask> {

	public ShardSyncTaskMorphia() {}

	public ShardSyncTaskMorphia(ShardSyncTask syncTask) { super(syncTask); }
}
