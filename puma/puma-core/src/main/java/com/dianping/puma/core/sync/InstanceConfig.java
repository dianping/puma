package com.dianping.puma.core.sync;

import java.util.ArrayList;
import java.util.List;

public class InstanceConfig {

    private List<DatabaseConfig> databases = new ArrayList<DatabaseConfig>();

    public List<DatabaseConfig> getDatabases() {
        return databases;
    }

    public void setDatabases(List<DatabaseConfig> databases) {
        this.databases = databases;
    }

    public void addDatabase(DatabaseConfig database) {
        this.databases.add(database);
    }

    @Override
    public String toString() {
        return "Instance [databases=" + databases + "]";
    }

}
