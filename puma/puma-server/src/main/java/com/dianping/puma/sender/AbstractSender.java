package com.dianping.puma.sender;

import org.apache.log4j.Logger;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.common.Notifiable;
import com.dianping.puma.common.NotifyService;
import com.dianping.puma.core.event.ChangedEvent;

public abstract class AbstractSender implements Sender, Notifiable {
	private static final Logger	log				= Logger.getLogger(AbstractSender.class);
	protected String			name;
	protected int				maxTryTimes		= 3;
	protected boolean			canMissEvent	= false;
	protected volatile boolean	stop			= false;
	protected NotifyService		notifyService;

	/**
	 * @param notifyService
	 *            the notifyService to set
	 */
	public void setNotifyService(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

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

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {
		stop = true;
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
		long retryCount = 0;
		while (true) {
			if (stop) {
				break;
			}
			try {
				doSend(event, context);
				break;
			} catch (Exception e) {
				if (retryCount++ > maxTryTimes) {
					if (canMissEvent) {
						log.error("[Miss]Send event(" + event + ") failed for " + maxTryTimes + " times.");
						if (this.notifyService != null) {
							this.notifyService.alarm("[Miss]Send event(" + event + ") failed for " + maxTryTimes
									+ " times.", e, false);
						}
						return;
					} else {
						if (retryCount % 100 == 0) {
							log.error("Send event(" + event + ") failed for " + retryCount + " times.");
							if (this.notifyService != null) {
								this.notifyService.alarm("Send event(" + event + ") failed for " + maxTryTimes
										+ " times and this event can't miss.", e, true);
							}
						}
					}
				}
				Thread.sleep(((retryCount % 15) + 1) * 300);
			}
		}
	}

	protected abstract void doSend(ChangedEvent event, PumaContext context) throws Exception;
}
