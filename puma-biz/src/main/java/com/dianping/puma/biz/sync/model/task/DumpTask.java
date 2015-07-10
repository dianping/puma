package com.dianping.puma.biz.sync.model.task;

import java.util.Arrays;
import java.util.List;

import com.dianping.puma.biz.entity.sync.mapping.DumpMapping;


public class DumpTask extends Task {

    private static final long serialVersionUID = 1174973531845800248L;

    //Dump配置：映射配置(DumpConfig)
    private DumpMapping dumpMapping;

    private List<String> options = Arrays.asList(new String[] { "--no-autocommit", "--disable-keys", "--quick",
            "--add-drop-database=false","--no-create-info", "--add-drop-table=false", "--skip-add-locks", "--default-character-set=utf8",
            "--max_allowed_packet=16777216", "--net_buffer_length=16384", "-i", "--master-data=2", "--single-transaction", "--hex-blob" });

    public DumpTask() {
        super(Type.DUMP);
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
        return "DumpTask [dumpMapping=" + dumpMapping + ", options=" + options + ", toString()=" + super.toString() + "]";
    }

}
