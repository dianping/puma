package com.dianping.puma.alarm.core.model;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/28.
 * Email: lixiaotian07@gmail.com
 */
@ToString
public class AlarmContext {

    private String namespace;

    private String name;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
