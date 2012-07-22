package com.dianping.puma.core;

import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;

import com.dianping.puma.core.event.DdlEvent;

public class Test {
	public static void main(String[] args) throws Exception {
		ObjectMapper om = new ObjectMapper();
		DdlEvent event = new DdlEvent();
		event.setDatabase("db");
		event.setExecuteTime(111);
		event.setSeq(11);
		event.setSql("sql");
		event.setTable("tb");
		System.out.println(Arrays.asList(om.writeValueAsString(event).getBytes()));
	}
}
