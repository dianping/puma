package com.dianping.puma.core.entity;

import com.dianping.puma.core.constant.SyncType;
import com.google.code.morphia.annotations.Entity;

import java.util.Arrays;
import java.util.List;

@Entity
public class ShardDumpTask extends BaseSyncTask {

    public ShardDumpTask() {
        this.setSyncType(SyncType.SHARD_DUMP);
    }

    private String srcDbName;

    private String dstDbName;

    private String dataBase;

    private String targetDataBase;

    private String tableName;

    private String targetTableName;

    private String shardRule;

    private String indexColumnName;

    private long indexKey;

    private long indexIncrease = 1000000;

    private long maxKey;

    private List<String> options = Arrays.asList("--master-data=2", "--disable-keys", "--skip-comments", "--quick",
            "--add-drop-database=false", "--no-create-info", "--add-drop-table=false", "--skip-add-locks",
            "--default-character-set=utf8", "--max_allowed_packet=1073741824",
            "-i", "--single-transaction", "--hex-blob", "--compact");

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getShardRule() {
        return shardRule;
    }

    public void setShardRule(String shardRule) {
        this.shardRule = shardRule;
    }

    public String getSrcDbName() {
        return srcDbName;
    }

    public void setSrcDbName(String srcDbName) {
        this.srcDbName = srcDbName;
    }

    public long getIndexKey() {
        return indexKey;
    }

    public void setIndexKey(long indexKey) {
        this.indexKey = indexKey;
    }

    public long getMaxKey() {
        return maxKey;
    }

    public void setMaxKey(long maxKey) {
        this.maxKey = maxKey;
    }

    public String getDstDbName() {
        return dstDbName;
    }

    public void setDstDbName(String dstDbName) {
        this.dstDbName = dstDbName;
    }

    public String getDataBase() {
        return dataBase;
    }

    public void setDataBase(String dataBase) {
        this.dataBase = dataBase;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexColumnName() {
        return indexColumnName;
    }

    public void setIndexColumnName(String indexColumnName) {
        this.indexColumnName = indexColumnName;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public String getTargetDataBase() {
        return targetDataBase;
    }

    public void setTargetDataBase(String targetDataBase) {
        this.targetDataBase = targetDataBase;
    }

    public long getIndexIncrease() {
        return indexIncrease;
    }

    public void setIndexIncrease(long indexIncrease) {
        this.indexIncrease = indexIncrease;
    }
}
