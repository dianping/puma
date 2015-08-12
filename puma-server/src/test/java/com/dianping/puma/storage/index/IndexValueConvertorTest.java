package com.dianping.puma.storage.index;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.puma.storage.Sequence;

/**
 * 
 * @author damonzhu
 *
 */
public class IndexValueConvertorTest {

	@Test
	public void testConvertor() {
		IndexValueConvertor convertor = new IndexValueConvertor();

		IndexValueImpl l2Index = new IndexValueImpl();
		l2Index.setIndexKey(new IndexKeyImpl(1L, "mysql-binlog.0000001", 4L));
		l2Index.setTable("receipt");
		l2Index.setDdl(false);
		l2Index.setTransactionBegin(true);
		l2Index.setTransactionCommit(false);
		l2Index.setDml(true);
		l2Index.setSequence(new Sequence(123123L, 1));

		byte[] bytes = convertor.convertToObj(l2Index);
		
		System.out.println(bytes.length);
		System.out.println(new String(bytes));

		IndexValueImpl convertFromObj = convertor.convertFromObj(bytes);

		Assert.assertEquals("mysql-binlog.0000001", convertFromObj.getIndexKey().getBinlogFile());
		Assert.assertEquals(4L, convertFromObj.getIndexKey().getBinlogPosition());
		Assert.assertEquals(1L, convertFromObj.getIndexKey().getServerId());
		Assert.assertEquals("receipt", convertFromObj.getTable());
		Assert.assertEquals(false, convertFromObj.isDdl());
		Assert.assertEquals(true, convertFromObj.isDml());
		Assert.assertEquals(true, convertFromObj.isTransaction());
		Assert.assertEquals(true, convertFromObj.isTransactionBegin());
		Assert.assertEquals(false, convertFromObj.isTransactionCommit());
		Assert.assertEquals(123123L, convertFromObj.getSequence().longValue());
		Assert.assertEquals(1, convertFromObj.getSequence().getLen());
	}
}
