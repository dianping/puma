package com.dianping.puma.admin.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.cache.exception.CacheException;
import com.dianping.puma.admin.common.StateContainer;
import com.dianping.puma.core.model.AbstractAck;
import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ClientRelatedInfo;
import com.dianping.puma.core.model.ServerAck;

@Component("stateCacheService")
public class StateCacheService {

	private static final Logger LOG = LoggerFactory.getLogger(StateCacheService.class);

	private static final String CLIENT_CACHE_SUFFIX = "_client";
	private static final String SERVER_CACHE_SUFFIX = "_server";
	private static final String PUMA_CLIENTACK_STATE_CACHE = "PumaClientAckStateCache";
	@Autowired
	private CacheService cacheService;
	@Autowired
	private StateContainer stateContainer;

	public void ayncSetKeyValue(String key, AbstractAck serverAck) {
		CacheKey cacheKey = new CacheKey(PUMA_CLIENTACK_STATE_CACHE, key);
		try {
			LOG.info("pumaCache!");
			cacheService.asyncSet(cacheKey, serverAck);
		} catch (CacheException e) {
			LOG.error("pumaCache failed, key : " + key, e);
		}
	}

	public AbstractAck getKeyValue(String key) {
		CacheKey cacheKey = new CacheKey(PUMA_CLIENTACK_STATE_CACHE, key);
		return cacheService.get(cacheKey);
	}

	public void pushAck() {
		Map<String, AtomicBoolean> isClientAckLastests = stateContainer.getIsClientAckLastests();
		Map<String, AtomicBoolean> isServerAckLastests = stateContainer.getIsServerAckLastests();
		Map<String, ClientAck> clientAcks = stateContainer.getClientAcks();
		Map<String, ServerAck> serverAcks = stateContainer.getServerAcks();
		for (Map.Entry<String, AtomicBoolean> isClientAckLastest : isClientAckLastests.entrySet()) {
			if (isClientAckLastest.getValue().get() && clientAcks.containsKey(isClientAckLastest.getKey())) {
				ayncSetKeyValue(isClientAckLastest.getKey() + CLIENT_CACHE_SUFFIX,
						clientAcks.get(isClientAckLastest.getKey()));
				stateContainer.setClientAckLastest(isClientAckLastest.getKey(), false);
				LOG.info("####write Client ack info to cache. clientName: " + isClientAckLastest.getKey());
			}
		}

		for (Map.Entry<String, AtomicBoolean> isServerAckLastest : isServerAckLastests.entrySet()) {
			if (isServerAckLastest.getValue().get() && serverAcks.containsKey(isServerAckLastest.getKey())) {
				ayncSetKeyValue(isServerAckLastest.getKey() + SERVER_CACHE_SUFFIX,
						serverAcks.get(isServerAckLastest.getKey()));
				stateContainer.setServerAckLastest(isServerAckLastest.getKey(), false);
				LOG.info("####write Server ack info to cache. clientName: " + isServerAckLastest.getKey());
			}
		}
	}

	public ClientAck popClientAck(String clientName) {
		return (ClientAck) getKeyValue(clientName + CLIENT_CACHE_SUFFIX);
	}

	public List<ClientAck> popClientAcks(List<String> clientNames) {
		List<ClientAck> clientAcks = new ArrayList<ClientAck>();
		for (String clientName : clientNames) {
			clientAcks.add(popClientAck(clientName));
		}
		return clientAcks;
	}

	public ServerAck popServerAck(String clientName) {
		return (ServerAck) getKeyValue(clientName + SERVER_CACHE_SUFFIX);
	}

	public List<ServerAck> popServerAcks(List<String> clientNames) {
		List<ServerAck> serverAcks = new ArrayList<ServerAck>();
		for (String clientName : clientNames) {
			serverAcks.add(popServerAck(clientName));
		}
		return serverAcks;
	}

	public ClientRelatedInfo getClientRelatedInfo(String clientName) {
		ClientRelatedInfo clientRelatedInfo = new ClientRelatedInfo();
		ClientAck clientAck = popClientAck(clientName);
		ServerAck serverAck = popServerAck(clientName);
		clientRelatedInfo.setClientAck(clientAck);
		clientRelatedInfo.setServerAck(serverAck);
		return clientRelatedInfo;
	}

	public Map<String, ClientRelatedInfo> getClientRelatedInfos(List<String> clientNames) {
		List<ClientAck> clientAcks = popClientAcks(clientNames);
		Map<String, ClientRelatedInfo> clientRelatedInfos = new HashMap<String, ClientRelatedInfo>();
		for (ClientAck clientAck : clientAcks) {
			ClientRelatedInfo clientRelatedInfo = new ClientRelatedInfo();
			clientRelatedInfo.setClientAck(clientAck);
			clientRelatedInfos.put(clientAck.getClientName(), clientRelatedInfo);
		}
		List<ServerAck> serverAcks = popServerAcks(clientNames);
		for (ServerAck serverAck : serverAcks) {
			if (clientRelatedInfos.containsKey(serverAck.getClientName())) {
				clientRelatedInfos.get(serverAck.getClientName()).setServerAck(serverAck);
			}
		}
		return clientRelatedInfos;
	}
}
