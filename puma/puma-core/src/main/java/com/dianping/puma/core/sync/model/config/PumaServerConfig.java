package com.dianping.puma.core.sync.model.config;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity
public class PumaServerConfig {
    @Id
    private ObjectId id;

    //数据库名称(如Dianping)
    @Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
    private String mysqlName;
    //puma-server列表(host)
    private List<String> hosts;
    //puma-server的target
    private String target;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMysqlName() {
        return mysqlName;
    }

    public void setMysqlName(String mysqlName) {
        this.mysqlName = mysqlName;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> pumaServerHosts) {
        this.hosts = pumaServerHosts;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
