package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncServerEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/db/*.xml")
public class SyncServerDaoTest {

	@Autowired
	SyncServerDao syncServerDao;

	@Test
	public void testFind() throws Exception {
	}

	@Test
	public void testInsert() throws Exception {
		SyncServerEntity entity = new SyncServerEntity();
		entity.setName("test");
		entity.setHost("127.0.0.1");
		entity.setPort(8080);

		syncServerDao.insert(entity);
	}

	@Test
	public void testUpdate() throws Exception {
	}

	@Test
	public void testDelete() throws Exception {

	}
}