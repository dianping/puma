package com.dianping.puma.syncserver.web;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.core.sync.Config;
import com.dianping.puma.core.sync.Database;

public class DumpClient {
    private Config src;
    private Config dest;

    private List<Database> databases = new ArrayList<Database>();

    public Config getSrc() {
        return src;
    }

    public void setSrc(Config src) {
        this.src = src;
    }

    public Config getDest() {
        return dest;
    }

    public void setDest(Config dest) {
        this.dest = dest;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    @Override
    public String toString() {
        return "DumpClient [src=" + src + ", dest=" + dest + ", databases=" + databases + "]";
    }

    /**
     * 进行dump，并返回binlog位置
     */
    public Long dump() {
        return null;
        // TODO Auto-generated method stub

    }

}
