package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.old.PumaServer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/**/*.xml")
public class PumaServerTargetDaoTest {

	@Autowired
	PumaServerTargetDao pumaServerTargetDao;

	@Test
	public void test() {
		List<PumaServerTargetEntity> entities = pumaServerTargetDao.findByDatabase("abb");
		/*
		PumaServerTargetEntity entity = new PumaServerTargetEntity();
		entity.setServerName("123");
		entity.setTargetDb("123");
		entity.setBeginTime(new Date());
		pumaServerTargetDao.insert(entity);*/
	}
}