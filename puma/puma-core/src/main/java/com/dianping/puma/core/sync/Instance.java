package com.dianping.puma.core.sync;

import java.util.ArrayList;
import java.util.List;

public class Instance {

    private List<Database> databases = new ArrayList<Database>();

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
        return "Instance [databases=" + databases + "]";
    }

}
