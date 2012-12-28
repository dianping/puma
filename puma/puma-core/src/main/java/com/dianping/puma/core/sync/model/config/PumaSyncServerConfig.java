package com.dianping.puma.core.sync.model.config;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity
public class PumaSyncServerConfig {
    @Id
    private ObjectId id;
    //数据库名称(如Dianping)
    @Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
    private String name;
    //pumaSyncServer host
    private String host;

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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "PumaSyncServerConfig [id=" + id + ", name=" + name + ", host=" + host + "]";
    }

}
