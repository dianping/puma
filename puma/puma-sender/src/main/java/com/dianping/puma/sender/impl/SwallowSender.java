package com.dianping.puma.sender.impl;

import com.dianping.puma.client.DataChangedEvent;

public class SwallowSender extends AbstractAsyncSender {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.sender.impl.AbstractAsyncSender#doAsyncSend()
	 */
	@Override
	protected void doAsyncSend(DataChangedEvent dataChangedEvent) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(dataChangedEvent);
	}

}