package com.dianping.puma.biz.sync.model.config;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity
public class MysqlConfig {

    @Id
    private ObjectId id;
    //数据库名称(如Dianping)
    @Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
    private String name;
    //mysql配置列表(host，username，passwd)
    private List<MysqlHost> hosts;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MysqlHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<MysqlHost> hosts) {
        this.hosts = hosts;
    }

}
