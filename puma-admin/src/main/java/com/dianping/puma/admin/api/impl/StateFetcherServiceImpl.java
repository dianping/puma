package com.dianping.puma.admin.api.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.puma.admin.cache.StateCacheService;
import com.dianping.puma.core.api.StateFetcherService;
import com.dianping.puma.core.model.ClientRelatedInfo;

@Service
@Component
public class StateFetcherServiceImpl implements StateFetcherService {

	private static final Logger LOG = LoggerFactory.getLogger(StateFetcherServiceImpl.class);

	@Autowired
	private StateCacheService stateCacheService;

	@Override
	public ClientRelatedInfo getRelatedInfo(String clientName) {
		if (clientName == null) {
			return null;
		}
		LOG.info("Client get ClientName: {} related info.", clientName);
		return stateCacheService.getClientRelatedInfo(clientName);
	}

	@Override
	public Map<String, ClientRelatedInfo> getRelatedInfos(List<String> clientNames) {
		if (clientNames == null && clientNames.size() == 0) {
			return null;
		}
		LOG.info("Client get related infos.");
		return stateCacheService.getClientRelatedInfos(clientNames);
	}

}
