package com.dianping.puma.pumaserver.client;

import com.dianping.puma.common.model.ClientAck;
import com.dianping.puma.common.model.ClientConfig;
import com.dianping.puma.common.model.ClientConnect;
import com.dianping.puma.common.service.PumaClientAckService;
import com.dianping.puma.common.service.PumaClientConfigService;
import com.dianping.puma.common.service.PumaClientConnectService;
import com.dianping.puma.pumaserver.client.exception.PumaClientManageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class RemoteClientManager extends AbstractClientManager {

    @Autowired
    PumaClientAckService clientAckService;

    @Autowired
    PumaClientConfigService clientConfigService;

    @Autowired
    PumaClientConnectService clientConnectService;

    @Override
    public void addClientAck(String clientName, ClientAck clientAck) throws PumaClientManageException {
        try {
            clientAckService.update(clientName, clientAck);
        } catch (Throwable t) {
            throw new PumaClientManageException(
                    "Failed to add puma client[%s] ack[%s].", clientName, clientAck, t);
        }
    }

    @Override
    public void addClientConfig(String clientName, ClientConfig clientConfig)
            throws PumaClientManageException {
        try {
            clientConfigService.update(clientName, clientConfig);
        } catch (Throwable t) {
            throw new PumaClientManageException(
                    "Failed to add puma client[%s] config[%s]", clientName, clientConfig, t);
        }
    }

    @Override
    public void addClientConnect(String clientName, ClientConnect clientConnect)
            throws PumaClientManageException {
        try {
            clientConnectService.update(clientName, clientConnect);
        } catch (Throwable t) {
            throw new PumaClientManageException(
                    "Failed to add puma client[%s] connect[%s]", clientName, clientConnect, t);
        }
    }

    public void setClientAckService(PumaClientAckService clientAckService) {
        this.clientAckService = clientAckService;
    }

    public void setClientConfigService(PumaClientConfigService clientConfigService) {
        this.clientConfigService = clientConfigService;
    }

    public void setClientConnectService(PumaClientConnectService clientConnectService) {
        this.clientConnectService = clientConnectService;
    }
}
