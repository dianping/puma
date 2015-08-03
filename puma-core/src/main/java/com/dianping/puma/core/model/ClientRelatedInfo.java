package com.dianping.puma.core.model;

import java.io.Serializable;

public class ClientRelatedInfo implements Serializable{

	private static final long serialVersionUID = 2721382097815566812L;

	private ServerAck serverAck;
	
	private ClientAck clientAck;

	public ServerAck getServerAck() {
		return serverAck;
	}

	public void setServerAck(ServerAck serverAck) {
		this.serverAck = serverAck;
	}

	public ClientAck getClientAck() {
		return clientAck;
	}

	public void setClientAck(ClientAck clientAck) {
		this.clientAck = clientAck;
	}
}
