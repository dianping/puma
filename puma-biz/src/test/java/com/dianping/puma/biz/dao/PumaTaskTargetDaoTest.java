package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskTargetEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/common/*.xml")
public class PumaTaskTargetDaoTest {

	@Autowired
	PumaTaskTargetDao pumaTaskTargetDao;

	@Test
	public void test() {
		PumaTaskTargetEntity entity = new PumaTaskTargetEntity();
		entity.setTaskId(1);
		entity.setDatabase("test-database");
		entity.setTable("test-table");

		pumaTaskTargetDao.insert(entity);
	}
}