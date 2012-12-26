package com.dianping.puma.core.sync.model.mapping;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * dump规则：<br>
 * (1)如果目标没有数据库，则自动为目标数据库自动创建table，但database不自动创建。<br>
 * (2)允许db改名，table改名，不会自动修改字段名。 (3)要求table的结构(字段个数，对应字段的类型)一致。
 */
@Entity
public class DumpMapping {

    @Id
    private ObjectId id;

    private List<DatabaseMapping> databaseMappings;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<DatabaseMapping> getDatabaseMappings() {
        return databaseMappings;
    }

    public void setDatabaseMappings(List<DatabaseMapping> DatabaseMappings) {
        this.databaseMappings = DatabaseMappings;
    }

    @Override
    public String toString() {
        return "DumpMapping [id=" + id + ", databaseMappings=" + databaseMappings + "]";
    }

}
