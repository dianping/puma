package com.dianping.puma;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public enum ComponentContainer {
	SPRING;

	private static final String SPRING_CONFIG = "context-bootstrap.xml";

	private ApplicationContext ctx;

	private ComponentContainer() {
		this.ctx = new ClassPathXmlApplicationContext(SPRING_CONFIG);
	}

	@SuppressWarnings("unchecked")
	public <T> T lookup(String id) {
		return (T) this.ctx.getBean(id);
	}

	public <T> T lookup(String id, Class<T> type) {
		return this.ctx.getBean(id, type);
	}
}
