package com.dianping.puma.core.sync.model.task;

import com.google.code.morphia.annotations.Entity;

@Entity
public class CatchupTask extends AbstractTask {
    private static final long serialVersionUID = 9067626307003002897L;

    public CatchupTask() {
        super(Type.CATCHUP);
    }

}
