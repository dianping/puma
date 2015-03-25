package com.dianping.puma.core.entity;

import com.google.code.morphia.annotations.Entity;

import java.util.Arrays;
import java.util.List;

@Entity
public class ShardDumpTask extends DumpTask {

    //分库分表规则名
    private String ruleName;

    //每个规则下可能有多个逻辑表，每个任务只能选择一个逻辑表
    private String tableName;

    private List<String> options = Arrays.asList("--disable-keys", "--skip-comments", "--quick",
            "--add-drop-database=false", "--no-create-info", "--add-drop-table=false", "--skip-add-locks",
            "--default-character-set=utf8", "--max_allowed_packet=16777216", "--net_buffer_length=16384",
            "-i", "--master-data=2", "--single-transaction", "--hex-blob", "--extended-inser=false", "--compact");


    @Override
    public List<String> getOptions() {
        return options;
    }

    @Override
    public void setOptions(List<String> options) {
        this.options = options;
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
