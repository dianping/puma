package com.dianping.puma.sender.impl;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.filter.DefaultEventFilterChain;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainConfig;
import com.dianping.puma.sender.Sender;

public abstract class AbstractSender implements Sender {
	protected String					name;
	protected EventFilterChainConfig	filterChainConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.sender.Sender#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EventFilterChainConfig getFilterChainConfig() {
		return filterChainConfig;
	}

	public void setFilterChainConfig(EventFilterChainConfig filterChainConfig) {
		this.filterChainConfig = filterChainConfig;
	}

	@Override
	public void send(DataChangedEvent event, PumaContext context) throws Exception {
		EventFilterChain filterChain = new DefaultEventFilterChain();
		filterChain.setEventFilters(filterChainConfig.getEventFilters());
		if (filterChain.doNext(event, context)) {
			doSend(event, context);
		}
	}

	protected abstract void doSend(DataChangedEvent event, PumaContext context);
}
