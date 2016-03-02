package com.dianping.puma.filter;

import com.dianping.puma.core.event.RowChangedEvent;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class EventFilterChainFactoryTest {

	@Test
	public void testCreateEventFilterChain() {
		String[] dts = { "cat.table1", "cat.table2", "dog.*", "puma.ab* " };
		EventFilterChain filterChain = EventFilterChainFactory.createEventFilterChain(true, true, false, dts);
		RowChangedEvent event = new RowChangedEvent();
		event.setDatabase("cat");
		event.setTable("table1");
		Assert.assertTrue(filterChain.doNext(event));
		event.setTransactionBegin(true);
		filterChain.reset();
		Assert.assertFalse(filterChain.doNext(event));
		filterChain = EventFilterChainFactory.createEventFilterChain(true, false, true, dts);
		Assert.assertFalse(filterChain.doNext(event));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEventFilterChain2() throws Exception {
		String[] dts = { "cat.table1", "cat.table2", "dog.*", "puma.ab* " };
		EventFilterChain filterChain = EventFilterChainFactory.createEventFilterChain(true, true, false, dts);

		List<EventFilter> filters = (List<EventFilter>) getFieldValue(filterChain, "eventFilters");
		Assert.assertEquals(3, filters.size());
		Assert.assertTrue(filters.get(0) instanceof TransactionInfoEventFilter);
		TransactionInfoEventFilter tsFilter = (TransactionInfoEventFilter) filters.get(0);
		boolean needTsInfo = (Boolean) getFieldValue(tsFilter, "needTsInfo");
		Assert.assertFalse(needTsInfo);

		Assert.assertTrue(filters.get(1) instanceof DbTbEventFilter);
		DbTbEventFilter dbtbFilter = (DbTbEventFilter) filters.get(1);
		Map<String, Boolean> dbtbMap = (Map<String, Boolean>) getFieldValue(dbtbFilter, "dbtbMap");
		Assert.assertEquals(5, dbtbMap.size());
		List<String> tbPrefixList = (List<String>) getFieldValue(dbtbFilter, "tbPrefixList");
		Assert.assertEquals(2, tbPrefixList.size());
		Assert.assertEquals("dog.", tbPrefixList.get(0));
		Assert.assertEquals("puma.ab", tbPrefixList.get(1));

		Assert.assertTrue(filters.get(2) instanceof DmlDdlEventFilter);
		DmlDdlEventFilter ddeventfilter = (DmlDdlEventFilter) filters.get(2);
		int operation = (Integer) getFieldValue(ddeventfilter, "operationType");
		Assert.assertEquals(3, operation);

	}

	private Object getFieldValue(Object instance, String fieldName) throws Exception {
		Field declaredField = instance.getClass().getDeclaredField(fieldName);
		declaredField.setAccessible(true);
		return declaredField.get(instance);
	}

}
