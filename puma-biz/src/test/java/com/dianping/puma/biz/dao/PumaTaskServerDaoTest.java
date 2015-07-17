package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskServerEntity;
import com.dianping.puma.core.constant.ActionController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/common/*.xml")
public class PumaTaskServerDaoTest {

	@Autowired
	PumaTaskServerDao pumaTaskServerDao;

	@Test
	public void test() {
		PumaTaskServerEntity entity = new PumaTaskServerEntity();
		entity.setTaskId(10);
		entity.setServerId(11);
		entity.setActionController(ActionController.START);

		pumaTaskServerDao.insert(entity);
	}
}