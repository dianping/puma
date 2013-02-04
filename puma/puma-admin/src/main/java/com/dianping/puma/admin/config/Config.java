package com.dianping.puma.admin.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.InitializingBean;

public class Config implements InitializingBean {

    private static Config instance;

    public static Config getInstance() {
        return instance;
    }

    @PostConstruct
    public void init() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

}
