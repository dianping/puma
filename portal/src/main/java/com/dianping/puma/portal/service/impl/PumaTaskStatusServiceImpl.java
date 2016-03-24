package com.dianping.puma.portal.service.impl;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.common.model.PumaServer;
import com.dianping.puma.common.service.PumaServerService;
import com.dianping.puma.core.util.ConvertHelper;
import com.dianping.puma.portal.model.DashboardModel;
import com.dianping.puma.portal.model.PumaServerStatusDto;
import com.dianping.puma.portal.service.PumaTaskStatusService;
import com.dianping.puma.portal.visitor.Impl.DashboardVisitor;
import com.dianping.puma.portal.visitor.Impl.MonitorVisitor;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 15/8/24
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Component
public class PumaTaskStatusServiceImpl implements PumaTaskStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStatusServiceImpl.class);

    private final MonitorVisitor monitorVisitor = new MonitorVisitor();

    @Autowired
    private PumaServerService pumaServerService;

    private final static String PUMA_STATUS_LION_KEY = "puma.server.status.path";

    protected ConfigCache configCache = ConfigCache.getInstance();

    protected volatile HttpAsyncClient httpClient;

    @PostConstruct
    public void init() {
        CloseableHttpAsyncClient client = HttpAsyncClients
                .custom()
                .setDefaultRequestConfig(RequestConfig
                        .custom()
                        .setConnectTimeout(5 * 1000)
                        .setSocketTimeout(10 * 1000)
                        .build())
                .build();
        client.start();
        httpClient = client;
    }

    private volatile long lastAccessTime;

    private volatile long lastReloadTime;

    private volatile Map<String, PumaServerStatusDto> status = new ConcurrentHashMap<String, PumaServerStatusDto>();

    public DashboardModel getDashboard() {
        lastAccessTime = System.currentTimeMillis();
        DashboardVisitor visitor = new DashboardVisitor();
        visitor.visit(status.values());
        return visitor.getDashboardModel();
    }

    public Map<String, PumaServerStatusDto> getAllStatus() {
        lastAccessTime = System.currentTimeMillis();
        return status;
    }

    public PumaServerStatusDto getStatusByName(String name) {
        lastAccessTime = System.currentTimeMillis();
        return status.get(name);
    }

    @Scheduled(fixedDelay = 4000)
    public void collect() {
        if (System.currentTimeMillis() - lastAccessTime > 5 * 60 * 1000 &&
                System.currentTimeMillis() - lastReloadTime < 5 * 60 * 1000) {
            return;
        }
        try {
            List<PumaServer> pumaServers = pumaServerService.findAllAlive();
            cleanUp(pumaServers);

            for (final PumaServer server : pumaServers) {
                try {
                    queryStatus(server);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            lastReloadTime = System.currentTimeMillis();

            monitorVisitor.visit(status.values());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected void queryStatus(final PumaServer server) {
        HttpGet get = new HttpGet(buildUrl(server.getHost()));
        get.releaseConnection();
        httpClient.execute(get, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                try {
                    PumaServerStatusDto dto = ConvertHelper.fromJson(EntityUtils.toString(response.getEntity()), PumaServerStatusDto.class);
                    dto.setName(server.getName());
                    status.put(server.getName(), dto);
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            @Override
            public void failed(Exception e) {
                PumaServerStatusDto oldServer = status.get(server.getName());
                if (oldServer != null) {
                    oldServer.setGenerateTime(System.currentTimeMillis());
                }
                LOG.error(e.getMessage(), e);
            }

            @Override
            public void cancelled() {

            }
        });
    }

    protected void cleanUp(List<PumaServer> servers) {
        Set<String> serverNames = FluentIterable.from(servers).transform(new Function<PumaServer, String>() {
            @Override
            public String apply(PumaServer pumaServer) {
                return pumaServer.getName();
            }
        }).toSet();

        Iterator<Map.Entry<String, PumaServerStatusDto>> iterator = status.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PumaServerStatusDto> entry = iterator.next();
            if (!serverNames.contains(entry.getKey())) {
                iterator.remove();
            }
        }
    }

    protected String buildUrl(String url) {
        String template = configCache.getProperty(PUMA_STATUS_LION_KEY);
        return String.format(template, url);
    }
}