package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.LoadMerger;
import com.dianping.puma.syncserver.job.load.model.BatchRows;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class LoadMergerTest {

	BatchRows batchRows = new BatchRows();
	BatchRows expected = new BatchRows();
	RowChangedEvent row0 = new RowChangedEvent();
	RowChangedEvent row1 = new RowChangedEvent();
	RowChangedEvent row2 = new RowChangedEvent();

	@Before
	public void before() {
		batchRows.clear();
		expected.clear();

		row0.setDMLType(DMLType.INSERT);
		row0.setDatabase("puma");
		row0.setTable("test");
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap0.put("name", new ColumnInfo(false, null, "Linda"));
		row0.setColumns(columnInfoMap0);
		batchRows.replace(row0);

		row1.setDMLType(DMLType.DELETE);
		row1.setDatabase("puma");
		row1.setTable("test");
		Map<String, ColumnInfo> columnInfoMap1 = new HashMap<String, ColumnInfo>();
		columnInfoMap1.put("id", new ColumnInfo(true, 1, null));
		columnInfoMap1.put("name", new ColumnInfo(false, "Linda", null));
		row1.setColumns(columnInfoMap1);
		batchRows.replace(row1);

		row2.setDMLType(DMLType.UPDATE);
		row2.setDatabase("puma");
		row2.setTable("test");
		Map<String, ColumnInfo> columnInfoMap2 = new HashMap<String, ColumnInfo>();
		columnInfoMap2.put("id", new ColumnInfo(true, 2, 2));
		columnInfoMap2.put("name", new ColumnInfo(false, "James", "Linda"));
		row2.setColumns(columnInfoMap2);
		batchRows.replace(row2);
	}

	@Test
	public void mergeInsertTest0() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.INSERT);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap.put("name", new ColumnInfo(false, null, "Helen"));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row);
		expected.replace(row1);
		expected.replace(row2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeInsertTest1() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.INSERT);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, null, 1));
		columnInfoMap.put("name", new ColumnInfo(false, null, "Helen"));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row0);
		expected.replace(row);
		expected.replace(row2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeInsertTest2() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.INSERT);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, null, 2));
		columnInfoMap.put("name", new ColumnInfo(false, null, "Helen"));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row0);
		expected.replace(row1);
		expected.replace(row);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeDeleteTest0() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.DELETE);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, 0, null));
		columnInfoMap.put("name", new ColumnInfo(false, "Helen", null));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row);
		expected.replace(row1);
		expected.replace(row2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeDeleteTest1() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.DELETE);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, 1, null));
		columnInfoMap.put("name", new ColumnInfo(false, "Helen", null));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row0);
		expected.replace(row);
		expected.replace(row2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeDeleteTest2() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.DELETE);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, 2, null));
		columnInfoMap.put("name", new ColumnInfo(false, "Helen", null));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row0);
		expected.replace(row1);
		expected.replace(row);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeUpdateTest0() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.UPDATE);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, 0, 0));
		columnInfoMap.put("name", new ColumnInfo(false, "Helen", "Mary"));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		row.setDMLType(DMLType.INSERT);
		expected.replace(row);
		expected.replace(row1);
		expected.replace(row2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeUpdateTest1() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.UPDATE);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, 1, 1));
		columnInfoMap.put("name", new ColumnInfo(false, "Helen", "Mary"));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row0);
		expected.replace(row);
		expected.replace(row2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeUpdateTest2() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.UPDATE);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, 2, 2));
		columnInfoMap.put("name", new ColumnInfo(false, "Helen", "Mary"));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		expected.replace(row0);
		expected.replace(row1);
		expected.replace(row);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

	@Test
	public void mergeUpdateTest3() {
		RowChangedEvent row = new RowChangedEvent();

		row.setDMLType(DMLType.UPDATE);
		row.setDatabase("puma");
		row.setTable("test");
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		columnInfoMap.put("id", new ColumnInfo(true, 0, 1));
		columnInfoMap.put("name", new ColumnInfo(false, "Helen", "Mary"));
		row.setColumns(columnInfoMap);

		LoadMerger.merge(row, batchRows);
		RowChangedEvent tmpRow0 = row.clone();
		tmpRow0.setDMLType(DMLType.INSERT);
		RowChangedEvent tmpRow1 = row.clone();
		tmpRow0.setDMLType(DMLType.DELETE);
		expected.replace(tmpRow0);
		expected.replace(tmpRow1);
		expected.replace(row2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, batchRows));
	}

}
