package com.dianping.puma.core.sync.model.task;

import java.util.Arrays;
import java.util.List;

import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.google.code.morphia.annotations.Entity;

@Entity
public class DumpTask extends Task {

    private static final long serialVersionUID = 1174973531845800248L;

    //    源数据库的具体host
    private MysqlHost srcMysqlHost;

    //Dump配置：映射配置(DumpConfig)
    private DumpMapping dumpMapping;

    private List<String> options = Arrays.asList(new String[] { "--no-autocommit", " --disable-keys", "--quick",
            "--add-drop-database=false", "--add-drop-table=false", "--skip-add-locks", "--default-character-set=utf8",
            "--max_allowed_packet=16777216", " --net_buffer_length=16384", "-i", "--master-data=2", "--single-transaction" });

    public DumpTask() {
        super(Type.DUMP);
    }

    public MysqlHost getSrcMysqlHost() {
        return srcMysqlHost;
    }

    public void setSrcMysqlHost(MysqlHost srcMysqlHost) {
        this.srcMysqlHost = srcMysqlHost;
    }

    public DumpMapping getDumpMapping() {
        return dumpMapping;
    }

    public void setDumpMapping(DumpMapping dumpMapping) {
        this.dumpMapping = dumpMapping;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "DumpTask [srcMysqlHost=" + srcMysqlHost + ", dumpMapping=" + dumpMapping + ", options=" + options + ", toString()="
                + super.toString() + "]";
    }

}
