package com.dianping.puma.core.sync.model.task;

import com.google.code.morphia.annotations.Entity;

@Entity
public class SyncTask extends AbstractTask {

    private static final long serialVersionUID = 2359517002901314187L;

    public SyncTask() {
        super(Type.SYNC);
    }

}
