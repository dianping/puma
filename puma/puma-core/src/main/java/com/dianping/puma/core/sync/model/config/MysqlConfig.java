package com.dianping.puma.core.sync.model.config;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity
public class MysqlConfig {

    //数据库名称(如Dianping)
    @Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
    private String name;
    //mysql配置列表(host，username，passwd)
    private List<MysqlHost> hosts;

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
