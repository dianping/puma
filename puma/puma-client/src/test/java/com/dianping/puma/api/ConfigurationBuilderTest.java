/**
 * Project: puma-client
 * 
 * File Created at 2012-7-17
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/**
 * TODO Comment of ConfigurationBuilderTest
 * 
 * @author Leo Liang
 * 
 */
public class ConfigurationBuilderTest {
	@Test
	public void test() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.codecType("111");
		builder.ddl(true);
		builder.dml(false);
		builder.host("111.3.3");
		builder.name("test");
		builder.port(123);
		builder.seqFileBase("11111");
		builder.tables("cat", "a", "b*");
		builder.tables("me", "d");
		builder.target("fff");
		builder.transaction(true);
		builder.timeStamp(111);
		builder.binlog("ffff.fff");
		builder.binlogPos(1234);
		builder.serverId(223);
		Configuration conf = builder.build();
		Assert.assertEquals("111", conf.getCodecType());
		Assert.assertEquals(true, conf.isNeedDdl());
		Assert.assertEquals(false, conf.isNeedDml());
		Assert.assertEquals("111.3.3", conf.getHost());
		Assert.assertEquals("test", conf.getName());
		Assert.assertEquals(123, conf.getPort());
		Assert.assertEquals("11111", conf.getSeqFileBase());
		Assert.assertEquals("fff", conf.getTarget());
		Assert.assertEquals(true, conf.isNeedTransactionInfo());
		Assert.assertEquals(111, conf.getTimeStamp());
		Assert.assertEquals(1234, conf.getBinlogPos());
		Assert.assertEquals(223, conf.getServerId());
		Assert.assertEquals("ffff.fff", conf.getBinlog());
		Map<String, List<String>> databaseTablesMapping = conf.getDatabaseTablesMapping();
		Assert.assertEquals(2, databaseTablesMapping.size());
		Assert.assertEquals(Arrays.asList(new String[] { "a", "b*" }), databaseTablesMapping.get("cat"));
		Assert.assertEquals(Arrays.asList(new String[] { "d" }), databaseTablesMapping.get("me"));
	}
}
