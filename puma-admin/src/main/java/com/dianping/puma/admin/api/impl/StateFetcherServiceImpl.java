package com.dianping.puma.admin.api.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.puma.admin.cache.StateCacheService;
import com.dianping.puma.core.api.StateFetcherService;
import com.dianping.puma.core.model.ClientRelatedInfo;

@Service
@Component
public class StateFetcherServiceImpl implements StateFetcherService {

	@Autowired
	private StateCacheService stateCacheService;
	
	@Override
	public ClientRelatedInfo getRelatedInfo(String clientName) {
		return stateCacheService.getClientRelatedInfo(clientName);
	}

	@Override
	public Map<String, ClientRelatedInfo> getRelatedInfos(List<String> clientNames) {
		return stateCacheService.getClientRelatedInfos(clientNames);
	}

}
