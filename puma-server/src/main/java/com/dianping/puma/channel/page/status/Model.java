package com.dianping.puma.channel.page.status;

import com.dianping.puma.channel.ChannelPage;
import com.dianping.puma.common.SystemStatusContainer;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ChannelPage, Action, Context> {
	private SystemStatusContainer	systemStatus;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	/**
	 * @return the systemStatus
	 */
	public SystemStatusContainer getSystemStatus() {
		return systemStatus;
	}

	/**
	 * @param systemStatus
	 *            the systemStatus to set
	 */
	public void setSystemStatus(SystemStatusContainer systemStatus) {
		this.systemStatus = systemStatus;
	}
}
