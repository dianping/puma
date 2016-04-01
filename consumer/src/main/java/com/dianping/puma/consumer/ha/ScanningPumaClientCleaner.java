package com.dianping.puma.consumer.ha;

import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.NamedThreadFactory;
import com.dianping.puma.consumer.exception.PumaClientCleanException;
import com.dianping.puma.consumer.model.ClientToken;
import com.dianping.puma.consumer.service.PumaClientTokenService;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/4/1.
 * Email: lixiaotian07@gmail.com
 */
public class ScanningPumaClientCleaner extends AbstractPumaLifeCycle implements PumaClientCleaner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaClientTokenService pumaClientTokenService;

    private long scanIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("scanning-puma-client-cleaner-executor"));

    private List<PumaClientCleanable> cleanables;

    private ConcurrentMap<String, ClientToken> clientTokenMap = new MapMaker().makeMap();

    @Override
    public void start() {
        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    scan();
                } catch (Throwable t) {
                    logger.error("Failed to periodically scan puma client cleaning.", t);
                }
            }
        }, 0, scanIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        super.stop();

        executor.shutdownNow();
    }

    private void scan() {
        Iterator<Map.Entry<String, ClientToken>> iterable = clientTokenMap.entrySet().iterator();
        while (iterable.hasNext()) {
            Map.Entry<String, ClientToken> entry = iterable.next();
            String clientName = entry.getKey();
            ClientToken localClientToken = entry.getValue();
            ClientToken globalClientToken = pumaClientTokenService.find(clientName);
            if (!localClientToken.equals(globalClientToken)) {
                iterable.remove();
                clean(clientName);
            }
        }
    }

    @Override
    public void registerClientToken(String clientName, ClientToken clientToken) throws PumaClientCleanException {
        pumaClientTokenService.update(clientName, clientToken);
        clientTokenMap.put(clientName, clientToken);
    }

    @Override
    public void clean(String clientName) throws PumaClientCleanException {
        for (PumaClientCleanable cleanable: cleanables) {
            cleanable.clean(clientName);
        }

        clientTokenMap.remove(clientName);
    }

    public void setPumaClientTokenService(PumaClientTokenService pumaClientTokenService) {
        this.pumaClientTokenService = pumaClientTokenService;
    }

    public void setScanIntervalInSecond(long scanIntervalInSecond) {
        this.scanIntervalInSecond = scanIntervalInSecond;
    }

    public void setCleanables(List<PumaClientCleanable> cleanables) {
        this.cleanables = cleanables;
    }
}
