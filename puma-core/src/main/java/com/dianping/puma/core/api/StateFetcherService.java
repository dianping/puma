package com.dianping.puma.core.api;

import java.util.List;
import java.util.Map;

import com.dianping.puma.core.model.ClientRelatedInfo;

public interface StateFetcherService {
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
