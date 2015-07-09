package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/*.xml")
public class PumaServerDaoTest {

	@Autowired
	PumaServerDao pumaServerDao;

	@Test
	public void testFind() throws Exception {
	}

	@Test
	public void testInsert() throws Exception {
		PumaServerEntity entity = new PumaServerEntity();
		entity.setName("test-name");
		entity.setHost("test-host");
		entity.setPort(8080);

		pumaServerDao.insert(entity);
	}
}