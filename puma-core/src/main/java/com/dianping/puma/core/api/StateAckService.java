package com.dianping.puma.core.api;

import java.util.List;

import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ServerAck;

public interface StateAckService {
	
	/*
	 * puma Server 定时发送的Ack信息
	 * 
	 */
	void setServerAck(ServerAck serverAck);
	
	/*
	 * puma Server 定时发送的Ack集合信息
	 * 
	 */
	void setServerAcks(List<ServerAck> serverAcks);
	
	/*
	 * puma Client 定时发送的Ack信息
	 * 
	 */
	void setClientAck(ClientAck clientAck);
	
	/*
	 * puma Client 拉取最新的Ack信息
	 * 
	 */
	ClientAck getClientAck(String clientName);
	
}
