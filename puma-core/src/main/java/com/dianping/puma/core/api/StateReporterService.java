package com.dianping.puma.core.api;

import java.util.List;
import java.util.Map;

import com.dianping.puma.core.model.ClientRelatedInfo;
import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ServerAck;

public interface StateReporterService {
	
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
	ClientRelatedInfo getRelatedInfo(String clientName);
	
	/*
	 * 反馈的相关信息
	 * 
	 */
	Map<String,ClientRelatedInfo> getRelatedInfos(List<String> clientNames);
}
