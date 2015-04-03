package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.ShardSyncTask;
import com.google.code.morphia.annotations.Entity;

@Entity("ShardDumpTask")
public class ShardDumpTaskMorphia extends BaseMorphiaEntity<ShardDumpTask> {

	public ShardDumpTaskMorphia() {}

	public ShardDumpTaskMorphia(ShardDumpTask shardDumpTask) { super(shardDumpTask); }
}
