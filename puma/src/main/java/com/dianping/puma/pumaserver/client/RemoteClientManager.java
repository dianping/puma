package com.dianping.puma.pumaserver.client;

import com.dianping.puma.biz.model.ClientConfig;
import com.dianping.puma.biz.model.ClientConnect;
import com.dianping.puma.biz.service.ClientConfigService;
import com.dianping.puma.biz.service.ClientConnectService;
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
    ClientConfigService clientConfigService;

    @Autowired
    ClientConnectService clientConnectService;

    @Override
    public void putConfig(String clientName, ClientConfig clientConfig) throws PumaClientManageException {
        try {
            clientConfigService.replace(clientName, clientConfig);
        } catch (Throwable t) {
            throw new PumaClientManageException(
                    "Failed to put puma client[%s] config[%s]", clientName, clientConfig);
        }
    }

    @Override
    public void putConnect(String clientName, ClientConnect clientConnect) throws PumaClientManageException {
        try {
            clientConnectService.replace(clientName, clientConnect);
        } catch (Throwable t) {
            throw new PumaClientManageException(
                    "Failed to put puma client[%s] connect[%s]", clientName, clientConnect);
        }
    }

    @Override
    public void remove(String clientName) throws PumaClientManageException {
        try {
            clientConfigService.remove(clientName);
            clientConnectService.remove(clientName);
        } catch (Throwable t) {
            throw new PumaClientManageException(
                    "Failed to remove puma client[%s] config and connect.", clientName);
        }
    }

    public void setClientConfigService(ClientConfigService clientConfigService) {
        this.clientConfigService = clientConfigService;
    }

    public void setClientConnectService(ClientConnectService clientConnectService) {
        this.clientConnectService = clientConnectService;
    }
}
