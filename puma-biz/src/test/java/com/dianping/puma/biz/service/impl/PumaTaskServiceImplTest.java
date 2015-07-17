package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.service.PumaTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/common/*.xml")
public class PumaTaskServiceImplTest {

	@Autowired
	PumaTaskService pumaTaskService;

	@Test
	public void testFindByPumaServerName() {
		List<PumaTaskEntity> entities = pumaTaskService.findByPumaServerName("test-server");
	}
}