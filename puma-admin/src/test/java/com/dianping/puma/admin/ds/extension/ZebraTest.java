package com.dianping.puma.admin.ds.extension;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZebraTest {

	Zebra zebra = new Zebra();

	@Before
	public void before() {

	}

	@Test
	public void testParseGroupDsName() {
		String groupDsPath0 = "groupds.puma.mapping";
		String expected0 = "puma";
		String result0 = zebra.parseGroupDsName(groupDsPath0);
		assertEquals(expected0, result0);

		String groupDsPath1 = "groupds.puma.test.mapping";
		String expected1 = "puma.test";
		String result1 = zebra.parseGroupDsName(groupDsPath1);
		assertEquals(expected1, result1);
	}

	@Test
	public void testParseIp() throws UnirestException {
		String jdbcUrl0 = "jdbc:mysql://127.0.0.1:3306/test?";
		String expected0 = "127.0.0.1:3306";
		String result0 = zebra.parseIp(jdbcUrl0);
		assertEquals(expected0, result0);

		String jdbcUrl1 = "jdbc:mysql://10.1.1.1:3306/DianPingLog?characterEncoding=UTF8";
		String expected1 = "10.1.1.1:3306";
		String result1 = zebra.parseIp(jdbcUrl1);
		assertEquals(expected1, result1);
	}
}