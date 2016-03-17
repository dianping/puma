package com.dianping.puma.pumaserver.client;

import com.dianping.puma.common.service.ClientConfigService;
import com.dianping.puma.common.service.ClientConnectService;
import com.dianping.puma.biz.service.ClientPositionService;
import com.dianping.puma.common.model.ClientAck;
import com.dianping.puma.common.model.ClientConfig;
import com.dianping.puma.common.model.ClientConnect;
import com.dianping.puma.common.service.ClientAckService;
import com.dianping.puma.common.service.ClientService;
import com.dianping.puma.common.utils.NamedThreadFactory;
import com.dianping.puma.pumaserver.client.exception.PumaClientManageException;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
@Service("asyncRemoteClientManager")
public class AsyncRemoteClientManager extends AbstractClientManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ClientAckService clientAckService;

    @Autowired
    ClientConfigService clientConfigService;

    @Autowired
    ClientConnectService clientConnectService;

    @Autowired
    ClientPositionService clientPositionService;

    @Autowired
    ClientService clientService;

    private ConcurrentMap<String, ClientAck> clientAckMap = new MapMaker().makeMap();

    private ConcurrentMap<String, ClientConfig> clientConfigMap = new MapMaker().makeMap();

    private ConcurrentMap<String, ClientConnect> clientConnectMap = new MapMaker().makeMap();

    private ScheduledExecutorService executor
            = Executors.newScheduledThreadPool(1, new NamedThreadFactory(getClass() + "-Pool", true));

    private long flushIntervalInSecond = 5;

    @Override
    protected void doStart() {
        super.doStart();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flushClientAck(clientAckMap);
                    flushClientConfig(clientConfigMap);
                    flushClientConnect(clientConnectMap);
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush puma client info.", t);
                }
            }
        }, 0, flushIntervalInSecond, TimeUnit.SECONDS);
    }

    private void flushClientAck(Map<String, ClientAck> clientAckMap) {
        Iterator<Map.Entry<String, ClientAck>> it = clientAckMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ClientAck> entry = it.next();
            String clientName = entry.getKey();
            ClientAck clientAck = entry.getValue();
            try {
                clientAckService.replace(clientName, clientAck);
                it.remove();
            } catch (Throwable t) {
                logger.error("Failed to flush puma client[{}] ack[{}].", clientName, clientAck, t);
            }
        }
    }

    private void flushClientConfig(Map<String, ClientConfig> clientConfigMap) {
        Iterator<Map.Entry<String, ClientConfig>> it = clientConfigMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ClientConfig> entry = it.next();
            String clientName = entry.getKey();
            ClientConfig clientConfig = entry.getValue();
            try {
                clientConfigService.replace(clientName, clientConfig);
                it.remove();
            } catch (Throwable t) {
                logger.error("Failed to flush puma client[{}] config[{}].", clientName, clientConfig, t);
            }
        }
    }

    private void flushClientConnect(Map<String, ClientConnect> clientConnectMap) {
        Iterator<Map.Entry<String, ClientConnect>> it = clientConnectMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ClientConnect> entry = it.next();
            String clientName = entry.getKey();
            ClientConnect clientConnect = entry.getValue();
            try {
                clientConnectService.replace(clientName, clientConnect);
                it.remove();
            } catch (Throwable t) {
                logger.error("Failed to flush puma client[{}] connect[{}].", clientName, clientConnect, t);
            }
        }
    }

    @Override
    protected void doStop() {
        super.doStop();

        try {
            executor.shutdownNow();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void addClientAck(String clientName, ClientAck clientAck) throws PumaClientManageException {
        clientAckMap.put(clientName, clientAck);
    }

    @Override
    public void addClientConfig(String clientName, ClientConfig clientConfig) throws PumaClientManageException {
        clientConfigMap.put(clientName, clientConfig);
    }

    @Override
    public void addClientConnect(String clientName, ClientConnect clientConnect) throws PumaClientManageException {
        clientConnectMap.put(clientName, clientConnect);
    }

    public void setClientAckService(ClientAckService clientAckService) {
        this.clientAckService = clientAckService;
    }

    public void setClientConfigService(ClientConfigService clientConfigService) {
        this.clientConfigService = clientConfigService;
    }

    public void setClientConnectService(ClientConnectService clientConnectService) {
        this.clientConnectService = clientConnectService;
    }

    public void setFlushIntervalInSecond(long flushIntervalInSecond) {
        this.flushIntervalInSecond = flushIntervalInSecond;
    }
}
