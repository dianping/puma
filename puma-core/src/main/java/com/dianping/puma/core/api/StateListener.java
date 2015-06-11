package com.dianping.puma.core.api;

import com.dianping.puma.core.model.RelatedInfo;
import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ServerAck;

public interface StateListener {
	
	/*
	 * puma Server 定时发送的Ack信息
	 * 
	 */
	void setServerAck(ServerAck serverAck);
	
	/*
	 * puma Client 定时发送的Ack信息
	 * 
	 */
	void setClientAck(ClientAck clientAck);
	
	/*
	 * 反馈的相关信息
	 * 
	 */
	RelatedInfo getRelatedInfo(String clientName);
	
}
