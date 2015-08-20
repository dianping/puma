package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTargetEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/**/*.xml")
public class PumaTargetDaoTest {

	@Autowired
	PumaTargetDao pumaTargetDao;

	@Test
	@Ignore
	public void test() {
		PumaTargetEntity entity = new PumaTargetEntity();
		entity.setDatabase("test");
		entity.setTable("test1+test2+test3");
		pumaTargetDao.insert(entity);
	}
}