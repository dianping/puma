package com.dianping.puma.sender.impl;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.monitor.BinlogInfoAware;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.filter.DefaultEventFilterChain;
import com.dianping.puma.sender.filter.EventFilterChain;
import com.dianping.puma.sender.filter.EventFilterChainConfig;

public abstract class AbstractSender implements Sender, BinlogInfoAware {
	protected String					name;
	protected EventFilterChainConfig	filterChainConfig;
	protected int						maxTryTimes		= 3;
	protected boolean					canMissEvent	= false;
	protected volatile boolean			stop			= false;

	/**
	 * @return the maxTryTimes
	 */
	public int getMaxTryTimes() {
		return maxTryTimes;
	}

	/**
	 * @param maxTryTimes
	 *            the maxTryTimes to set
	 */
	public void setMaxTryTimes(int maxTryTimes) {
		this.maxTryTimes = maxTryTimes;
	}

	/**
	 * @return the canMissEvent
	 */
	public boolean isCanMissEvent() {
		return canMissEvent;
	}

	/**
	 * @param canMissEvent
	 *            the canMissEvent to set
	 */
	public void setCanMissEvent(boolean canMissEvent) {
		this.canMissEvent = canMissEvent;
	}

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
