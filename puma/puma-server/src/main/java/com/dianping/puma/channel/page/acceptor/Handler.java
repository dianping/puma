package com.dianping.puma.channel.page.acceptor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	private static final Logger log = Logger.getLogger(Handler.class);

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "acceptor")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "acceptor")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Payload payload = ctx.getPayload();
		HttpServletResponse res = ctx.getHttpServletResponse();

		log.info("Client(" + payload.getClientName() + ") connected.");

		EventCodec codec = EventCodecFactory.createCodec(payload.getCodecType());
		EventFilterChain filterChain = EventFilterChainFactory.createEventFilterChain(payload.isDdl(), payload.isDml(),
		      payload.isNeedsTransactionMeta(), payload.getDatabaseTables());

		res.setContentType("application/octet-stream");
		res.addHeader("Connection", "Keep-Alive");

		long seq = payload.getSeq();
		EventStorage storage = ComponentContainer.SPRING.lookup("storage-" + payload.getTarget(), EventStorage.class);
		EventChannel channel = storage.getChannel(seq);

		while (true) {
			try {
				filterChain.reset();
				ChangedEvent event = channel.next();
				if (filterChain.doNext(event)) {
					byte[] data = codec.encode(event);
					res.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
					res.getOutputStream().write(data);
					res.getOutputStream().flush();
				}
			} catch (Exception e) {
				log.info("Client(" + payload.getClientName() + ") failed.");
				return;
			}
		}
	}
}