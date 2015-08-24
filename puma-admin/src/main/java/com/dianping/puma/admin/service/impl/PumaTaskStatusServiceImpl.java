package com.dianping.puma.admin.service.impl;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.admin.model.PumaServerStatusDto;
import com.dianping.puma.admin.service.PumaTaskStatusService;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.core.util.GsonUtil;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Dozer @ 15/8/24
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Component
public class PumaTaskStatusServiceImpl implements PumaTaskStatusService {

    @Autowired
    private PumaServerService pumaServerService;

    private final static String PUMA_STATUS_LION_KEY = "puma.server.status.path";

    protected ConfigCache configCache = ConfigCache.getInstance();

    protected HttpClient httpClient = HttpClients.createDefault();

    private volatile long lastAccessTime;

    private volatile long lastReloadTime;

    private Map<String, PumaServerStatusDto> status = ImmutableMap.of();

    public Map<String, PumaServerStatusDto> getAllStatus() {
        lastAccessTime = System.currentTimeMillis();
        return status;
    }

    public PumaServerStatusDto getStatusByName(String name) {
        lastAccessTime = System.currentTimeMillis();
        return status.get(name);
    }

    @Scheduled(fixedDelay = 1000)
    public void collect() {
        if (System.currentTimeMillis() - lastAccessTime > 5 * 60 * 1000 &&
                System.currentTimeMillis() - lastReloadTime < 5 * 60 * 1000) {
            return;
        }

        List<PumaServerEntity> servers = pumaServerService.findAllAlive();

        ImmutableMap.Builder<String, PumaServerStatusDto> builder = ImmutableMap.builder();

        for (PumaServerEntity server : servers) {
            try {
                HttpGet get = new HttpGet(buildUrl(server.getHost()));
                HttpResponse response = httpClient.execute(get);
                String json = EntityUtils.toString(response.getEntity());
                PumaServerStatusDto dto = GsonUtil.fromJson(json, PumaServerStatusDto.class);
                builder.put(server.getName(), dto);
            } catch (Exception e) {
                Cat.logError("Reload Puma Task Status Failed! " + server.getHost(), e);
            }
        }

        status = builder.build();
        lastReloadTime = System.currentTimeMillis();
    }

    protected String buildUrl(String url) {
        String template = configCache.getProperty(PUMA_STATUS_LION_KEY);
        return String.format(template, url);
    }
}