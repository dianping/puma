package com.dianping.puma.parser;

import com.dianping.puma.parser.mysql.utils.MySQLUtils;

import junit.framework.TestCase;

public class MySQLUtilsTest extends TestCase {

	public void testToTimestamp() {
		System.out.println(MySQLUtils.toTimestamp(1));
		System.out.println(MySQLUtils.toTimestamp(1).getTime());
		System.out.println(new java.sql.Timestamp(0).getTime());
		System.out.println(new java.sql.Timestamp(0).getNanos());
		System.out.println(new java.sql.Timestamp(1000));
	}
}
