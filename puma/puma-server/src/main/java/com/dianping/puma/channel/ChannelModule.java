package com.dianping.puma.channel;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "channel", defaultInboundAction = "acceptor", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.puma.channel.page.acceptor.Handler.class,

com.dianping.puma.channel.page.status.Handler.class,

com.dianping.puma.channel.page.admin.Handler.class
})
public class ChannelModule extends AbstractModule {

}
