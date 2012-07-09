package com.dianping.puma.channel;

import com.site.web.mvc.AbstractModule;
import com.site.web.mvc.annotation.ModuleMeta;
import com.site.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "channel", defaultInboundAction = "acceptor", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.puma.channel.page.acceptor.Handler.class,

com.dianping.puma.channel.page.status.Handler.class,

com.dianping.puma.channel.page.admin.Handler.class
})
public class ChannelModule extends AbstractModule {

}
