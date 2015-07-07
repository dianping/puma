package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.core.constant.ActionOperation;
import org.springframework.stereotype.Service;

@Service("syncTaskOperationReporter")
public class SyncTaskOperationReporter {


    public void report(String syncServerName, String taskName, ActionOperation operation) {
    }
}
