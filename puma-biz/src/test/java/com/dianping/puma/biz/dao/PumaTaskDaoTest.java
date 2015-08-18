package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/db/*.xml")
public class PumaTaskDaoTest {

	@Autowired
	PumaTaskDao pumaTaskDao;

	@Test
	public void test() {
		List<PumaTaskEntity> entities = pumaTaskDao.findByPage(0, 100);
		System.out.println(entities);
	}

}