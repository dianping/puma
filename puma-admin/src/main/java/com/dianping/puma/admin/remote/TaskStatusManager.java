package com.dianping.puma.admin.remote;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.model.state.TaskState;
import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 7/8/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public abstract class TaskStatusManager<T extends TaskState> {
    private static final Logger logger = LoggerFactory.getLogger(TaskStatusManager.class);

    protected Map<String, T> status = new ConcurrentHashMap<String, T>();

    public Collection<T> findAll() {
        return status.values();
    }

    public T find(String name) {
        return status.get(name);
    }

    protected abstract List<String> getUrlList();

    public String getKey(T entity) {
        return entity.getName();
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void refresh() {
        RestTemplate restTemplate = new RestTemplate();
        for (String url : getUrlList()) {
            try {
                ResponseEntity<String> json = restTemplate.getForEntity(URI.create(url), String.class);
                List<T> entity = GsonUtil.gson.fromJson(json.getBody(), new TypeToken<List<T>>() {
                }.getType());

                for (T item : entity) {
                    add(item);
                }
            } catch (Exception e) {
                logger.error(url + " fetch error!", e);
            }
        }
    }

    public void add(T item) {
        status.put(getKey(item), item);
    }

    public void remove(String name) {
        status.remove(name);
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void cleanup() {

    }
}
