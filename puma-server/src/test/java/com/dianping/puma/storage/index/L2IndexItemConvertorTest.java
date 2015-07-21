package com.dianping.puma.storage.index;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.puma.storage.Sequence;

/**
 * 
 * @author damonzhu
 *
 */
public class L2IndexItemConvertorTest {

	@Test
	public void testConvertor() {
		L2IndexItemConvertor convertor = new L2IndexItemConvertor();

		L2Index l2Index = new L2Index();
		l2Index.setBinlogIndexKey(new BinlogIndexKey("mysql-binlog.0000001", 4L, 1L));
		l2Index.setDatabase("dianping");
		l2Index.setTable("receipt");
		l2Index.setDdl(false);
		l2Index.setTransaction(true);
		l2Index.setDml(true);
		l2Index.setSequence(new Sequence(123123L,1));

		byte[] bytes = convertor.convertToObj(l2Index);

		System.out.println(bytes.length);
		System.out.println(new String(bytes));

		L2Index convertFromObj = convertor.convertFromObj(bytes);

		Assert.assertEquals("mysql-binlog.0000001", convertFromObj.getBinlogIndexKey().getBinlogFile());
		Assert.assertEquals(4L, convertFromObj.getBinlogIndexKey().getBinlogPos());
		Assert.assertEquals(1L, convertFromObj.getBinlogIndexKey().getServerId());
		Assert.assertEquals("dianping", convertFromObj.getDatabase());
		Assert.assertEquals("receipt", convertFromObj.getTable());
		Assert.assertEquals(false, convertFromObj.isDdl());
		Assert.assertEquals(true, convertFromObj.isDml());
		Assert.assertEquals(true, convertFromObj.isTransaction());
		Assert.assertEquals(123123L, convertFromObj.getSequence().longValue());
		Assert.assertEquals(1, convertFromObj.getSequence().getLen());
	}
}
