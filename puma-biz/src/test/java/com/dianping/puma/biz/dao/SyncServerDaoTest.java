package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncServerEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/common/*.xml")
public class SyncServerDaoTest {

	@Autowired
	SyncServerDao syncServerDao;

	@Test
	public void testFind() throws Exception {
	}

	@Test
	public void testInsert() throws Exception {
		SyncServerEntity entity = new SyncServerEntity();
		entity.setHost("127.0.0.1");

		syncServerDao.insert(entity);
	}

	@Test
	public void testUpdate() throws Exception {
	}

	@Test
	public void testDelete() throws Exception {

	}
}