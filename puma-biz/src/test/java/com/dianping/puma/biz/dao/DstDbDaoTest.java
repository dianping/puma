package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.DstDbEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/*.xml")
public class DstDbDaoTest {

	@Autowired
	DstDbDao dstDbDao;

	@Test
	public void testFind() throws Exception {

	}

	@Test
	public void testInsert() throws Exception {
		DstDbEntity entity = new DstDbEntity();
		entity.setJdbcRef("test-jdbcRef");
		entity.setHost("127.0.0.1");
		entity.setPort(8080);
		entity.setUsername("test-username");
		entity.setPassword("test-password");
		dstDbDao.insert(entity);
	}

	@Test
	public void testUpdate() throws Exception {

	}

	@Test
	public void testDelete() throws Exception {

	}
}