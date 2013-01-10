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

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.puma.core.constant.SubscribeConstant;

/**
 * TODO Comment of ConfigurationTest
 * 
 * @author Leo Liang
 * 
 */
public class ConfigurationTest {
    @Test
    public void testURL() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setHost("host");
        conf.setName("testConf");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setSeqFileBase("/test");
        conf.setTarget("target");
        Assert.assertEquals("http://host:123/puma/channel", conf.buildUrl());
    }

    @Test
    public void testParam() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setHost("host");
        conf.setName("testConf");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setSeqFileBase("/test");
        conf.setTarget("target");
        conf.addDatabaseTable("cat", "a", "b*");
        conf.addDatabaseTable("me", "d");
        conf.addDatabaseTable("me", "e");
        Assert.assertEquals(
                "seq=111&name=testConf&target=target&ddl=true&dml=false&ts=true&codec=hessian&dt=cat.a&dt=cat.b*&dt=me.d&dt=me.e",
                conf.buildRequestParamString(111));
    }

    @Test
    public void testWithBinlog() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setHost("host");
        conf.setName("testConf");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setSeqFileBase("/test");
        conf.setTarget("target");
        conf.addDatabaseTable("cat", "a", "b*");
        conf.addDatabaseTable("me", "d");
        conf.addDatabaseTable("me", "e");
        conf.setServerId(223);
        conf.setBinlog("fff");
        conf.setBinlogPos(345);
        Assert.assertEquals(
                "seq=-3&binlog=fff&binlogPos=345&serverId=223&name=testConf&target=target&ddl=true&dml=false&ts=true&codec=hessian&dt=cat.a&dt=cat.b*&dt=me.d&dt=me.e",
                conf.buildRequestParamString(SubscribeConstant.SEQ_FROM_BINLOGINFO));
    }

    @Test
    public void testWithTimeStamp() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setHost("host");
        conf.setName("testConf");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setSeqFileBase("/test");
        conf.setTarget("target");
        conf.addDatabaseTable("cat", "a", "b*");
        conf.addDatabaseTable("me", "d");
        conf.addDatabaseTable("me", "e");
        conf.setTimeStamp(12344);
        Assert.assertEquals(
                "seq=-4&timestamp=12344&name=testConf&target=target&ddl=true&dml=false&ts=true&codec=hessian&dt=cat.a&dt=cat.b*&dt=me.d&dt=me.e",
                conf.buildRequestParamString(SubscribeConstant.SEQ_FROM_TIMESTAMP));
    }

    @Test
    public void testValidateNoHost() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setName("testConf");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setSeqFileBase("/test");
        conf.setTarget("target");
        conf.addDatabaseTable("cat", "a", "b*");
        conf.addDatabaseTable("me", "d");
        conf.addDatabaseTable("me", "e");
        try {
            conf.validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testValidateNoName() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setHost("host");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setSeqFileBase("/test");
        conf.setTarget("target");
        conf.addDatabaseTable("cat", "a", "b*");
        conf.addDatabaseTable("me", "d");
        conf.addDatabaseTable("me", "e");
        try {
            conf.validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testValidateNoTarget() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setHost("host");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setName("name");
        conf.setSeqFileBase("/test");
        conf.addDatabaseTable("cat", "a", "b*");
        conf.addDatabaseTable("me", "d");
        conf.addDatabaseTable("me", "e");
        try {
            conf.validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testValidateNoDbTb() {
        Configuration conf = new Configuration();
        conf.setCodecType("hessian");
        conf.setHost("host");
        conf.setNeedDdl(true);
        conf.setNeedDml(false);
        conf.setNeedTransactionInfo(true);
        conf.setPort(123);
        conf.setName("name");
        conf.setTarget("ddd");
        conf.setSeqFileBase("/test");
        try {
            conf.validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
