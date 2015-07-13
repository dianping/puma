package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.MappingEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/*.xml")
public class MappingDaoTest {

	@Autowired
	MappingDao mappingDao;

	@Test
	public void testFind() throws Exception {

	}

	@Test
	public void testInsert() throws Exception {
		MappingEntity entity = new MappingEntity();
		entity.setDatabaseFrom("test-database-from");
		entity.setDatabaseTo("test-database-to");
		entity.setTableFrom("test-table-from");
		entity.setTableTo("test-table-to");
		entity.setColumnFrom("test-column-from");
		entity.setColumnTo("test-column-to");
		mappingDao.insert(entity);
	}

	@Test
	public void testUpdate() throws Exception {

	}

	@Test
	public void testDelete() throws Exception {

	}
}