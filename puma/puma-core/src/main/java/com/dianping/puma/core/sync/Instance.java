package com.dianping.puma.core.sync;

import java.util.ArrayList;
import java.util.List;

public class Instance {

    private Boolean ddl;
    private List<Database> databases = new ArrayList<Database>();

    public Boolean getDdl() {
        return ddl;
    }

    public void setDdl(Boolean ddl) {
        this.ddl = ddl;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public void addDatabase(Database database) {
        this.databases.add(database);
    }

    @Override
    public String toString() {
        return "Instance [ddl=" + ddl + ", databases=" + databases + "]";
    }

}
