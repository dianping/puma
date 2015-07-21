package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.MockTest;
import com.dianping.puma.biz.dao.PumaTaskTargetDao;
import com.dianping.puma.biz.entity.PumaTaskTargetEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class PumaServerServiceImplTest extends MockTest {

	PumaServerServiceImpl pumaServerService = new PumaServerServiceImpl();

	@Mock
	PumaTaskTargetDao pumaTaskTargetDao;

	@Before
	public void before() {
		pumaServerService.pumaTaskTargetDao = pumaTaskTargetDao;
	}

	@Test
	public void testFindByDatabaseAndTable() {
		List<PumaTaskTargetEntity> entities0 = new ArrayList<PumaTaskTargetEntity>();
		PumaTaskTargetEntity entity00 = new PumaTaskTargetEntity();
		entity00.setTaskId(0);
		entities0.add(entity00);
		PumaTaskTargetEntity entity01 = new PumaTaskTargetEntity();
		entity01.setTaskId(1);
		entities0.add(entity01);
		Mockito.when(pumaTaskTargetDao.findByDatabaseAndTable("test-db", "test-tb0")).thenReturn(entities0);

		List<PumaTaskTargetEntity> entities1 = new ArrayList<PumaTaskTargetEntity>();
		PumaTaskTargetEntity entity10 = new PumaTaskTargetEntity();
		entity10.setTaskId(1);
		entities1.add(entity10);
		PumaTaskTargetEntity entity11 = new PumaTaskTargetEntity();
		entity10.setTaskId(2);
		entities1.add(entity11);
		Mockito.when(pumaTaskTargetDao.findByDatabaseAndTable("test-db", "test-tb1")).thenReturn(entities1);

		List<String> tables = new ArrayList<String>();
		tables.add("test-tb0");
		tables.add("test-tb1");
		pumaServerService.findByDatabaseAndTables("test-db", tables);
	}
}
