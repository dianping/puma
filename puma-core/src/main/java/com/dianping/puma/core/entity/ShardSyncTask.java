package com.dianping.puma.core.entity;

import com.dianping.puma.core.constant.SyncType;
import com.google.code.morphia.annotations.Entity;

@Entity
public class ShardSyncTask extends BaseSyncTask {

    private static final long serialVersionUID = 1L;

    //分库分表规则名
    private String ruleName;

    //每个规则下可能有多个逻辑表，每个任务只能选择一个逻辑表
    private String tableName;

    //是否为迁移任务
    private boolean isMigrate;

    //迁移任务需要制定 binlogName
    private String binlogName;

    //迁移任务需要制定 binlogPos
    private long binlogPos;

    //可以制定 sequence
    private long seqTimestamp;

    public ShardSyncTask() {
        this.setSyncType(SyncType.SYNC.SHARD_SYNC);
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

    public boolean isMigrate() {
        return isMigrate;
    }

    public void setIsMigrate(boolean isMigrate) {
        this.isMigrate = isMigrate;
    }

    public String getBinlogName() {
        return binlogName;
    }

    public void setBinlogName(String binlogName) {
        this.binlogName = binlogName;
    }

    public long getBinlogPos() {
        return binlogPos;
    }

    public void setBinlogPos(long binlogPos) {
        this.binlogPos = binlogPos;
    }

    public long getSeqTimestamp() {
        return seqTimestamp;
    }

    public void setSeqTimestamp(long seqTimestamp) {
        this.seqTimestamp = seqTimestamp;
    }
}
