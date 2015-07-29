package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/db/*.xml")
public class PumaTaskStateDaoTest {

    @Autowired
    PumaTaskStateDao pumaTaskStateDao;

    @Test
    public void testDao() throws Exception {
        PumaTaskStateEntity entity = new PumaTaskStateEntity();
        entity.setTaskName("test-task-name");
        entity.setServerName("test-server-name");
        entity.setUpdateTime(new Date());
        entity.setStatus(Status.RUNNING);
        entity.setDetail("test-detail");
        entity.setBinlogInfo(new BinlogInfo());
        entity.setBinlogStat(new BinlogStat());
        pumaTaskStateDao.insert(entity);

        PumaTaskStateEntity result = pumaTaskStateDao.findByTaskNameAndServerName("test-task-name", "test-server-name");
        Assert.assertEquals(entity, result);
    }
}