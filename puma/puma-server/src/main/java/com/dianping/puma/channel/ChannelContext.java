package com.dianping.puma.channel;

import com.site.web.mvc.Action;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.Page;

public class ChannelContext<T extends ActionPayload<? extends Page, ? extends Action>> extends ActionContext<T> {

}
