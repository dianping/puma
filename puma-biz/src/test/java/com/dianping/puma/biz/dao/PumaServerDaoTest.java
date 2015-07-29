package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/common/*.xml")
public class PumaServerDaoTest {

	@Autowired
	PumaServerDao pumaServerDao;

	@Test
	@Ignore
	public void test() {
		PumaServerEntity entity = new PumaServerEntity();
		entity.setName("test-name");
		entity.setHost("127.0.0.1");
		entity.setPort(4040);
		entity.setUpdateTime(new Date());

		pumaServerDao.insert(entity);
	}
}