package com.dianping.puma.core.entity;

import com.google.code.morphia.annotations.Entity;

import java.util.Arrays;
import java.util.List;

@Entity
public class ShardDumpTask extends DumpTask {

    private String srcDbName;

    private String dstDbName;

    private String dataBase;

    private String tableName;

    private String shardRule;

    private long indexKey;

    private long maxKey;

    private List<String> options = Arrays.asList("--complete-insert", "--disable-keys", "--skip-comments", "--quick",
            "--add-drop-database=false", "--no-create-info", "--add-drop-table=false", "--skip-add-locks",
            "--default-character-set=utf8", "--max_allowed_packet=16777216", "--net_buffer_length=16384",
            "-i", "--single-transaction", "--hex-blob", "--extended-insert=false", "--compact");

    @Override
    public List<String> getOptions() {
        return options;
    }

    @Override
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
}
