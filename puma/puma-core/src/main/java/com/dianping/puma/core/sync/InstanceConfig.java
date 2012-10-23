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

    public List<DatabaseConfig> compare(InstanceConfig newInstanceConfig) {
        List<DatabaseConfig> oldDatabaseConfigs = new ArrayList<DatabaseConfig>(this.databases);
        List<DatabaseConfig> newDatabaseConfigs = new ArrayList<DatabaseConfig>(newInstanceConfig.getDatabases());
        //验证旧的database没有被删除
        for (DatabaseConfig oldDatabaseConfig : oldDatabaseConfigs) {
            boolean contain = false;
            for (DatabaseConfig newDatabaseConfig : newDatabaseConfigs) {
                if (oldDatabaseConfig.getFrom().equals(newDatabaseConfig.getFrom())
                        && oldDatabaseConfig.getTo().equals(newDatabaseConfig.getTo())) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                throw new IllegalArgumentException("<database>不能删除: " + oldDatabaseConfig);
            }
        }
        //老Config多出的database        
        oldDatabaseConfigs.removeAll(newDatabaseConfigs);
        List<DatabaseConfig> oldLeftDatabaseConfigs = oldDatabaseConfigs;
        //新Config多出的database
        newDatabaseConfigs.removeAll(this.databases);
        List<DatabaseConfig> newLeftDatabaseConfigs = newDatabaseConfigs;
        //从新老多出的部分，求出新增的database和修改的database(新增table)
        for (DatabaseConfig newDatabaseConfig : newLeftDatabaseConfigs) {
            //如果存在于老的之中，则是修改，那么仅保留新增的table
            for (DatabaseConfig oldDatabaseConfig : oldLeftDatabaseConfigs) {
                if (newDatabaseConfig.getFrom().equals(oldDatabaseConfig.getFrom())
                        && newDatabaseConfig.getTo().equals(oldDatabaseConfig.getTo())) {
                    List<TableConfig> oldTables = oldDatabaseConfig.getTables();
                    List<TableConfig> newTables = newDatabaseConfig.getTables();
                    //验证旧的table没有被删除
                    if (!newTables.containsAll(oldTables)) {
                        throw new IllegalArgumentException("<table>不能删除: " + oldDatabaseConfig);
                    }
                    newTables.removeAll(oldTables);
                    break;
                }
            }
        }
        return newLeftDatabaseConfigs;
    }

}
