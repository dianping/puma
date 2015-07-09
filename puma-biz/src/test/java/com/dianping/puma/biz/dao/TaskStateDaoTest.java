package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.TaskStateEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

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
        List<TaskStateEntity> result1 = taskStateDao.findByTaskNameAndTaskType("task1", "type1");
        List<TaskStateEntity> result2 = taskStateDao.findByTaskNameAndServerNameAndTaskType("task1", "server1", "type1");

        System.out.println("ok");
    }
}