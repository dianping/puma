package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogUnsubscription;
import com.dianping.puma.core.netty.entity.response.BinlogUnsubscriptionResponse;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.client.ClientType;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class BinlogUnsubscriptionHandler extends SimpleChannelInboundHandler<BinlogUnsubscription> {

	private ClientSessionService clientSessionService;

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogUnsubscription binlogUnsubscription) {
		ClientSession session = clientSessionService
				.get(binlogUnsubscription.getClientName(), binlogUnsubscription.getToken());

		// Destroy binlog channel.
		session.getBinlogChannel().destroy();

		// For browser user only.
		if (session.getClientType().equals(ClientType.BROSWER)) {
			BinlogUnsubscriptionResponse response = new BinlogUnsubscriptionResponse();
			response.setClientName(session.getClientName());
			response.setToken(session.getToken());
			response.setMsg("unsubscribe success");
			ctx.channel().writeAndFlush(response);
		}
	}

	public void setClientSessionService(ClientSessionService clientSessionService) {
		this.clientSessionService = clientSessionService;
	}
}
