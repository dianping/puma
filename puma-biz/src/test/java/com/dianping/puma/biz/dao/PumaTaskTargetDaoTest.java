package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTargetEntity;
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
		PumaTargetEntity entity = new PumaTargetEntity();
		entity.setTaskId(1);
		entity.setDatabase("test-database");
		entity.setTable("test-table");

		pumaTaskTargetDao.insert(entity);
	}
}