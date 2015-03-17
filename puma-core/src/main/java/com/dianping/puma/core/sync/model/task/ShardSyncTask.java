package com.dianping.puma.core.sync.model.task;

import com.google.code.morphia.annotations.Entity;

@Entity
public class ShardSyncTask extends AbstractTask {

    private static final long serialVersionUID = 1L;

    //分库分表规则名
    private String ruleName;

    //每个规则下可能有多个逻辑表，每个任务只能选择一个逻辑表
    private String tableName;

    public ShardSyncTask() {
        super(Type.SHARD_SYNC);
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
