package com.dianping.puma.core.sync.model.task;

import com.google.code.morphia.annotations.Entity;

@Entity
public class ShardSyncTask extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private String ruleName;

    public ShardSyncTask() {
        super(Type.SHARD_SYNC);
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
}
