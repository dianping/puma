package com.dianping.puma.channel.page.admin;

import com.dianping.puma.channel.ChannelPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

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
		m_page = ChannelPage.getByName(page, ChannelPage.ADMIN);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
