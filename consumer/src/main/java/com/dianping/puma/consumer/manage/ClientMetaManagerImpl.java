package com.dianping.puma.consumer.manage;

import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.consumer.exception.PumaClientMetaManageException;
import com.dianping.puma.consumer.model.ClientConfig;
import com.dianping.puma.consumer.model.ClientConnect;
import com.dianping.puma.consumer.model.ClientToken;
import com.dianping.puma.consumer.service.PumaClientConfigService;
import com.dianping.puma.consumer.service.PumaClientConnectService;
import com.dianping.puma.consumer.service.PumaClientTokenService;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/31.
 * Email: lixiaotian07@gmail.com
 */
public class ClientMetaManagerImpl extends AbstractPumaLifeCycle implements PumaClientMetaManager {

    private PumaClientTokenService pumaClientTokenService;

    private PumaClientConfigService pumaClientConfigService;

    private PumaClientConnectService pumaClientConnectService;

    private ConcurrentMap<String, ClientToken> clientTokenMap = new MapMaker().makeMap();

    private ConcurrentMap<String, ClientConfig> clientConfigMap = new MapMaker().makeMap();

    private ConcurrentMap<String, ClientConnect> clientConnectMap = new MapMaker().makeMap();

    @Override
    public ClientToken findClientToken(String clientName) throws PumaClientMetaManageException {
        return clientTokenMap.get(clientName);
    }

    @Override
    public ClientConfig findClientConfig(String clientName) throws PumaClientMetaManageException {
        return clientConfigMap.get(clientName);
    }

    @Override
    public ClientConnect findClientConnect(String clientName) throws PumaClientMetaManageException {
        return clientConnectMap.get(clientName);
    }

    @Override
    public void registerClientToken(String clientName, ClientToken clientToken) throws PumaClientMetaManageException {
        try {
            pumaClientTokenService.update(clientName, clientToken);
            clientTokenMap.put(clientName, clientToken);
        } catch (Throwable t) {
            throw new PumaClientMetaManageException("Failed to register client token[%s] for client[%s].",
                    clientToken, clientName, t);
        }
    }

    @Override
    public void registerClientConfig(String clientName, ClientConfig clientConfig) throws PumaClientMetaManageException {
        try {
            pumaClientConfigService.update(clientName, clientConfig);
            clientConfigMap.put(clientName, clientConfig);
        } catch (Throwable t) {
            throw new PumaClientMetaManageException("Failed to register client config[%s] for client[%s].",
                    clientConfig, clientName, t);
        }
    }

    @Override
    public void registerClientConnect(String clientName, ClientConnect clientConnect) throws PumaClientMetaManageException {
        try {
            pumaClientConnectService.update(clientName, clientConnect);
            clientConnectMap.put(clientName, clientConnect);
        } catch (Throwable t) {
            throw new PumaClientMetaManageException("Failed to register client connect[%s] for client[%s].",
                    clientConnect, clientName, t);
        }
    }

    @Override
    public void clean(String clientName) {
        clientTokenMap.remove(clientName);
        clientConfigMap.remove(clientName);
        clientConnectMap.remove(clientName);
    }

    public void setPumaClientTokenService(PumaClientTokenService pumaClientTokenService) {
        this.pumaClientTokenService = pumaClientTokenService;
    }

    public void setPumaClientConfigService(PumaClientConfigService pumaClientConfigService) {
        this.pumaClientConfigService = pumaClientConfigService;
    }

    public void setPumaClientConnectService(PumaClientConnectService pumaClientConnectService) {
        this.pumaClientConnectService = pumaClientConnectService;
    }
}
