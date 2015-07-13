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
    PumaServerDao pumaServerDao;

    @Test
    @Ignore
    public void testInsert() throws Exception {
        Object entity = pumaServerDao.count();
        System.out.println(new Gson().toJson(entity));
    }
}