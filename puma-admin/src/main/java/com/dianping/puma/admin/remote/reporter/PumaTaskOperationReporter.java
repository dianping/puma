package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.biz.event.entity.Event;
import com.dianping.puma.core.constant.ActionOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pumaTaskOperationReporter")
public class PumaTaskOperationReporter {

    @Autowired

    public void report(Event event) {
    }

    public void report(String pumaServerName, String taskName, ActionOperation operation) {
    }
}
