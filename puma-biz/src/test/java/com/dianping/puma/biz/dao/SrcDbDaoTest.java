package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SrcDbEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/spring/common/*.xml")
public class SrcDbDaoTest {

	@Autowired
	SrcDbDao srcDbDao;

	@Test
	public void test() {
		SrcDbEntity entity = new SrcDbEntity();
		entity.setName("test-name");
		entity.setJdbcRef("test-jdbcRef");
		entity.setHost("127.0.0.1");
		entity.setPort(3306);
		entity.setUsername("test-username");
		entity.setPassword("test-password");
		entity.setServerId(0);
		entity.setPreferred(false);

		srcDbDao.insert(entity);

		SrcDbEntity result = srcDbDao.findByName("test-name");
		Assert.assertTrue(EqualsBuilder.reflectionEquals(entity, result));
	}
}