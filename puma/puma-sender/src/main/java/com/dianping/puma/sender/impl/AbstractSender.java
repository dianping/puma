package com.dianping.puma.sender.impl;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.monitor.BinlogInfoAware;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.sender.Sender;

public abstract class AbstractSender implements Sender, BinlogInfoAware {
	protected String			name;
	protected int				maxTryTimes		= 3;
	protected boolean			canMissEvent	= false;
	protected volatile boolean	stop			= false;

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

	@Override
	public void send(ChangedEvent event, PumaContext context) throws Exception {
		doSend(event, context);
	}

	protected abstract void doSend(ChangedEvent event, PumaContext context);
}
