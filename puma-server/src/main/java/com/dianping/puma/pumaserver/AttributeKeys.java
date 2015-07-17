package com.dianping.puma.pumaserver;

import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;
import com.dianping.puma.pumaserver.client.ClientSession;
import io.netty.util.AttributeKey;

/**
 * Created by Dozer on 12/4/14.
 */
public interface AttributeKeys {
    AttributeKey<Boolean>        CLIENT_SUBSCRIBED  = AttributeKey.valueOf("Client.Subscribed");
    AttributeKey<ClientSession>     CLIENT_INFO        = AttributeKey.valueOf("Client.Info");
    AttributeKey<String>         CLIENT_NAME        = AttributeKey.valueOf("Client.Name");
    AttributeKey<AsyncBinlogChannel>  CLIENT_CHANNEL     = AttributeKey.valueOf("Client.Channel");
}
