package com.dianping.puma.admin.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ClientRelatedInfo;
import com.dianping.puma.core.model.ServerAck;

@Service("stateContainer")
public class StateContainer {
	
	private static final Logger LOG = LoggerFactory.getLogger(StateContainer.class);

	private ConcurrentMap<String, ServerAck> serverAcks = new ConcurrentHashMap<String, ServerAck>();

	private ConcurrentMap<String, ClientAck> clientAcks = new ConcurrentHashMap<String, ClientAck>();

	private ConcurrentMap<String, AtomicBoolean> isClientAckLastests = new ConcurrentHashMap<String, AtomicBoolean>();

	private ConcurrentMap<String, AtomicBoolean> isServerAckLastests = new ConcurrentHashMap<String, AtomicBoolean>();
	
	public void setClientAckInfo(ClientAck clientAck) {
		if (clientAck != null && StringUtils.isNotBlank(clientAck.getClientName())) {
			clientAcks.put(clientAck.getClientName(), clientAck);
			isClientAckLastests.put(clientAck.getClientName(), new AtomicBoolean(true));
		}

	}
	
	public void setServerAckInfo(ServerAck serverAck) {
		if (serverAck != null && StringUtils.isNotBlank(serverAck.getClientName())) {
			serverAcks.put(serverAck.getClientName(), serverAck);
			isServerAckLastests.put(serverAck.getClientName(), new AtomicBoolean(true));
		}
	}
	
	public void setClientAckLastest(String clientName,boolean isValid) {
		isClientAckLastests.put(clientName, new AtomicBoolean(isValid));
	}
	
	public void setServerAckLastest(String clientName,boolean isValid) {
		isServerAckLastests.put(clientName, new AtomicBoolean(isValid));
	}
	
	public Map<String,ServerAck> getServerAcks(){
		return Collections.unmodifiableMap(serverAcks);
	}
	
	public Map<String,ClientAck> getClientAcks(){
		return Collections.unmodifiableMap(clientAcks);
	}
	
	public Map<String,AtomicBoolean> isServerAckLastests(){
		return Collections.unmodifiableMap(isServerAckLastests);
	}
	
	public Map<String,AtomicBoolean> isClientAckLastests(){
		return Collections.unmodifiableMap(isClientAckLastests);
	}
	
	public Map<String,AtomicBoolean> getIsClientAckLastests(){
		return isClientAckLastests;
	}
	
	public Map<String,AtomicBoolean> getIsServerAckLastests(){
		return isServerAckLastests;
	}
	
	public List<ClientRelatedInfo> getClientRelatedInfos(List<String> clientNames){
		return new ArrayList<ClientRelatedInfo>();
	}
	
	public ClientRelatedInfo getClientRelatedInfo(String clientName){
		return new ClientRelatedInfo();
	}
	

}
