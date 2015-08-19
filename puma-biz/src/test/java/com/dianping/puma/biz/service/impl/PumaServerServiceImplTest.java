package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.MockTest;
import com.dianping.puma.biz.dao.PumaTaskTargetDao;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import org.junit.Before;
import org.junit.Test;
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
//		List<PumaTargetEntity> entities0 = new ArrayList<PumaTargetEntity>();
//		PumaTargetEntity entity00 = new PumaTargetEntity();
//		entity00.setTaskId(0);
//		entities0.add(entity00);
//		PumaTargetEntity entity01 = new PumaTargetEntity();
//		entity01.setTaskId(1);
//		entities0.add(entity01);
//		Mockito.when(pumaTaskTargetDao.findByDatabaseAndTable("test-db", "test-tb0")).thenReturn(entities0);
//
//		List<PumaTargetEntity> entities1 = new ArrayList<PumaTargetEntity>();
//		PumaTargetEntity entity10 = new PumaTargetEntity();
//		entity10.setTaskId(1);
//		entities1.add(entity10);
//		PumaTargetEntity entity11 = new PumaTargetEntity();
//		entity10.setTaskId(2);
//		entities1.add(entity11);
//		Mockito.when(pumaTaskTargetDao.findByDatabaseAndTable("test-db", "test-tb1")).thenReturn(entities1);
//
//		List<String> tables = new ArrayList<String>();
//		tables.add("test-tb0");
//		tables.add("test-tb1");
//		pumaServerService.findByDatabaseAndTables("test-db", tables);
	}
}
