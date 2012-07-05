/**
 * Project: puma-core
 * 
 * File Created at 2012-7-6
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
package com.dianping.puma.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;

/**
 * TODO Comment of EventTransportUtilsTest
 * 
 * @author Leo Liang
 * 
 */
public class EventTransportUtilsTest {
	@Test
	public void testDdl() throws IOException {
		DdlEvent event = new DdlEvent();
		event.setDatabase("testdb");
		event.setExecuteTime(11111111);
		event.setTable("testtb");
		event.setSeq(1233);
		event.setSql("SELECT * FROM testtb");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		EventTransportUtils.write(event, bos);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		DdlEvent result = (DdlEvent) EventTransportUtils.read(bis);
		Assert.assertEquals(event.getDatabase(), result.getDatabase());
		Assert.assertEquals(event.getExecuteTime(), result.getExecuteTime());
		Assert.assertEquals(event.getSeq(), result.getSeq());
		Assert.assertEquals(event.getSql(), result.getSql());
		Assert.assertEquals(event.getTable(), result.getTable());
	}

	@Test
	public void testDml() throws IOException {
		RowChangedEvent event = new RowChangedEvent();
		event.setDatabase("testdb");
		event.setExecuteTime(11111111);
		event.setTable("testtb");
		event.setSeq(1233);
		event.setActionType(RowChangedEvent.INSERT);
		event.setTransactionBegin(false);
		event.setTransactionCommit(false);
		Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
		columns.put("a", new ColumnInfo(false, "1", "2"));
		event.setColumns(columns);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		EventTransportUtils.write(event, bos);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		RowChangedEvent result = (RowChangedEvent) EventTransportUtils.read(bis);
		Assert.assertEquals(event.getDatabase(), result.getDatabase());
		Assert.assertEquals(event.getExecuteTime(), result.getExecuteTime());
		Assert.assertEquals(event.getSeq(), result.getSeq());
		Assert.assertEquals(event.getActionType(), result.getActionType());
		Assert.assertEquals(event.getTable(), result.getTable());
		Assert.assertEquals(event.isTransactionBegin(), result.isTransactionBegin());
		Assert.assertEquals(event.isTransactionCommit(), result.isTransactionCommit());
		for (Map.Entry<String, ColumnInfo> entry : event.getColumns().entrySet()) {
			Assert.assertEquals(entry.getValue().isKey(), result.getColumns().get(entry.getKey()).isKey());
			Assert.assertEquals(entry.getValue().getNewValue(), result.getColumns().get(entry.getKey()).getNewValue());
			Assert.assertEquals(entry.getValue().getOldValue(), result.getColumns().get(entry.getKey()).getOldValue());
		}
	}
}
