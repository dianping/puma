package com.dianping.puma.biz.dao;

import com.google.gson.Gson;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/*.xml")
public class DaoDebug {

    @Autowired
    PumaTaskDao dao;

    @Test
    @Ignore
    public void debug() throws Exception {
        Object entity = dao.findByPumaServerName("test-name");
        System.out.println(new Gson().toJson(entity));
    }
}