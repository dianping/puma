package com.dianping.puma.channel.page.status;

import com.dianping.puma.channel.ChannelPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ChannelPage, Action> {
	private ChannelPage m_page;

	@FieldMeta("op")
	private Action m_action;

	public void setAction(Action action) {
		m_action = action;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ChannelPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ChannelPage.getByName(page, ChannelPage.STATUS);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
