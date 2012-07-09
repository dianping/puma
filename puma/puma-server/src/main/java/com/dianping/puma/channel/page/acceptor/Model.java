package com.dianping.puma.channel.page.acceptor;

import com.dianping.puma.channel.ChannelPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ChannelPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
