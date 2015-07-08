package com.dianping.puma.admin.remote;

import com.dianping.puma.biz.entity.PumaServer;
import com.dianping.puma.biz.entity.PumaTask;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.core.model.state.PumaTaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Dozer @ 7/8/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Component
public class PumaTaskStatusManager extends TaskStatusManager<PumaTaskState> {

    @Autowired
    PumaServerService pumaServerService;

    public PumaTaskState find(PumaTask pumaTask) {
        return status.get(getKey(pumaTask.getName(), pumaTask.getPumaServerName()));
    }

    public void remove(PumaTask task) {
        remove(getKey(task.getName(), task.getPumaServerName()));
    }


    public String getKey(String name, String serverName) {
        return name + "&" + serverName;
    }

    @Override
    public String getKey(PumaTaskState entity) {
        return getKey(entity.getName(), entity.getServerName());
    }

    @Override
    protected List<String> getUrlList() {
        List<String> result = new ArrayList<String>();
        //todo: 只取10分钟内更新的
        for (PumaServer server : pumaServerService.findAll()) {
            result.add(String.format("http://%s:8080/status/puma-task", server.getHost()));
        }
        return result;
    }
}
