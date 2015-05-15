package com.dianping.puma.syncserver.job.transform;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.syncserver.MockTest;
import com.dianping.puma.syncserver.job.transform.exception.TransformException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class DefaultTransformerTest extends MockTest {

	DefaultTransformer transformer;

	@Mock
	MysqlMapping mysqlMapping;

	@Before
	public void before() {
		transformer = new DefaultTransformer();
		transformer.setName("puma");
		when(mysqlMapping.getSchema("puma_ori_schema")).thenReturn("puma_schema");
		when(mysqlMapping.getTable("puma_ori_table")).thenReturn("puma_table");
		when(mysqlMapping.getColumn("puma_ori_column")).thenReturn("puma_column");
		transformer.setMysqlMapping(mysqlMapping);
		transformer.start();
	}

	@After
	public void after() {
		transformer.stop();
	}

	@Test
	public void testTransformDml() {
		RowChangedEvent dmlEvent = new RowChangedEvent();
		dmlEvent.setDatabase("puma_ori_schema");
		dmlEvent.setTable("puma_ori_table");
		Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
		columns.put("puma_ori_column", new ColumnInfo(false, 1, 2));
		dmlEvent.setColumns(columns);

		RowChangedEvent expected = new RowChangedEvent();
		expected.setDatabase("puma_schema");
		expected.setTable("puma_table");
		Map<String, ColumnInfo> columns1 = new HashMap<String, ColumnInfo>();
		columns1.put("puma_column", new ColumnInfo(false, 1, 2));
		expected.setColumns(columns1);

		transformer.transform(dmlEvent);
		Assert.assertEquals(expected, dmlEvent);
	}

	@Test
	public void testTransformDdl() {
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("puma_ori_schema");
		ddlEvent.setTable("puma_ori_table");
		ddlEvent.setDDLType(DDLType.ALTER_TABLE);
		ddlEvent.setSql("ALTER TABLE puma_ori_table DROP i;");

		DdlEvent expected = new DdlEvent();
		expected.setDatabase("puma_schema");
		expected.setTable("puma_table");
		expected.setDDLType(DDLType.ALTER_TABLE);
		expected.setSql("ALTER TABLE puma_schema.puma_table DROP i;");
	}

	@Test(expected = TransformException.class)
	public void testTransformStopException() {
		RowChangedEvent dmlEvent = new RowChangedEvent();
		transformer.stop();
		transformer.transform(dmlEvent);
	}

	@Test(expected = TransformException.class)
	public void testTransformErrorException() {
		RowChangedEvent dmlEvent = new RowChangedEvent();
		dmlEvent.setDatabase("hello");
		transformer.transform(dmlEvent);
	}
}
