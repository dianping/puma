package com.dianping.puma.core.sync.model.mapping;

import java.util.ArrayList;
import java.util.List;

public class MysqlMapping  implements Cloneable {
    private List<DatabaseMapping> databases = new ArrayList<DatabaseMapping>();

    public List<DatabaseMapping> getDatabases() {
        return databases;
    }

    public void setDatabases(List<DatabaseMapping> databases) {
        this.databases = databases;
    }

    public void addDatabase(DatabaseMapping database) {
        this.databases.add(database);
    }

    @Override
    public MysqlMapping clone() throws CloneNotSupportedException {
        MysqlMapping dm = new MysqlMapping();
        List<DatabaseMapping> databases0 = new ArrayList<DatabaseMapping>();
        if (this.getDatabases() != null) {
            for (DatabaseMapping c : this.getDatabases()) {
                databases0.add(c.clone());
            }
        }
        dm.setDatabases(databases0);
        return dm;
    }

    @Override
    public String toString() {
        return "Instance [databases=" + databases + "]";
    }

    public MysqlMapping compare(MysqlMapping newMysqlMapping) throws CloneNotSupportedException {
        List<DatabaseMapping> oldDatabaseMappings = this.clone().getDatabases();
        List<DatabaseMapping> newDatabaseMappings = newMysqlMapping.clone().getDatabases();
        //验证旧的database没有被删除
        for (DatabaseMapping oldDatabaseMapping : oldDatabaseMappings) {
            boolean contain = false;
            for (DatabaseMapping newDatabaseMapping : newDatabaseMappings) {
                if (oldDatabaseMapping.getFrom().equals(newDatabaseMapping.getFrom())
                        && oldDatabaseMapping.getTo().equals(newDatabaseMapping.getTo())) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                throw new IllegalArgumentException("database不能删除: " + oldDatabaseMapping);
            }
        }
        //老Mapping多出的database        
        oldDatabaseMappings.removeAll(newDatabaseMappings);
        List<DatabaseMapping> oldLeftDatabaseMappings = oldDatabaseMappings;
        //新Mapping多出的database
        newDatabaseMappings.removeAll(this.databases);
        List<DatabaseMapping> newLeftDatabaseMappings = newDatabaseMappings;
        //从新老多出的部分，求出新增的database和修改的database(新增table)
        for (DatabaseMapping newDatabaseMapping : newLeftDatabaseMappings) {
            //如果存在于老的之中，则是修改，那么仅保留新增的table
            for (DatabaseMapping oldDatabaseMapping : oldLeftDatabaseMappings) {
                if (newDatabaseMapping.getFrom().equals(oldDatabaseMapping.getFrom())
                        && newDatabaseMapping.getTo().equals(oldDatabaseMapping.getTo())) {
                    List<TableMapping> oldTables = oldDatabaseMapping.getTables();
                    List<TableMapping> newTables = newDatabaseMapping.getTables();
                    //验证旧的table没有被删除
                    if (!newTables.containsAll(oldTables)) {
                        throw new IllegalArgumentException("table不能删除: " + oldDatabaseMapping);
                    }
                    newTables.removeAll(oldTables);
                    break;
                }
            }
        }
        MysqlMapping m = new MysqlMapping();
        m.setDatabases(newLeftDatabaseMappings);
        return m;
    }
}
