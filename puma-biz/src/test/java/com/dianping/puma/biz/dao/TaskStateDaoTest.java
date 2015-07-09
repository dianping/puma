package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.TaskStateEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/*.xml")
public class TaskStateDaoTest {
    @Autowired
    TaskStateDao taskStateDao;

    @Test
    @Ignore
    public void testDao() throws Exception {
        TaskStateEntity result2 = taskStateDao.findByTaskNameAndServerNameAndTaskType("task1", "server1", "type1").get(0);
        result2.getBinlogStat().setDdls(100l);
        taskStateDao.update(result2);

        result2.setTaskName("xxxxx2");
        taskStateDao.insert(result2);

        System.out.println("ok");
    }
}