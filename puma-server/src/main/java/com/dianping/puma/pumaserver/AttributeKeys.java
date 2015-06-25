package com.dianping.puma.pumaserver;

import com.dianping.puma.pumaserver.client.ClientInfo;
import io.netty.util.AttributeKey;

/**
 * Created by Dozer on 12/4/14.
 */
public interface AttributeKeys {
    AttributeKey<ClientInfo> CLIENT_INFO = AttributeKey.valueOf("Client.Info");
}
