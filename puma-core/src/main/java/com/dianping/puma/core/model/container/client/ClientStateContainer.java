package com.dianping.puma.core.model.container.client;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.container.AbstractStateContainer;
import com.dianping.puma.core.model.state.client.ClientState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//@Service("clientStateContainer")
public class ClientStateContainer extends AbstractStateContainer<ClientState> {

	public void setSeq(String name, long seq) {
		ClientState clientState = get(name);
		if (clientState != null) {
			clientState.setSeq(seq);
		}
	}

	public void setBinlog(String name, BinlogInfo binlogInfo) {
		ClientState clientState = get(name);
		if (clientState != null) {
			clientState.setBinlogInfo(binlogInfo);
		}
	}

	public List<ClientState> getByTaskName(String taskName) {
		List<ClientState> clientStates = new ArrayList<ClientState>();
		for (ClientState clientState: getAll()) {
			if (taskName.equals(clientState.getTaskName())) {
				clientStates.add(clientState);
			}
		}
		return clientStates;
	}
}
