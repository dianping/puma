package com.dianping.puma.admin.service.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.DumpActionStateService;
import com.dianping.puma.core.sync.dao.action.DumpActionStateDao;
import com.dianping.puma.core.sync.model.action.DumpActionState;
import com.google.code.morphia.Key;

@Service("dumpActionStateService")
public class DumpActionStateServiceImpl implements DumpActionStateService {
    @Autowired
    DumpActionStateDao dumpActionStateDao;

    @Override
    public ObjectId save(DumpActionState dumpActionState) {
        Key<DumpActionState> key = this.dumpActionStateDao.save(dumpActionState);
        this.dumpActionStateDao.getDatastore().ensureIndexes();
        return (ObjectId) key.getId();
    }

    @Override
    public DumpActionState find(ObjectId objectId) {
        return this.dumpActionStateDao.getDatastore().getByKey(DumpActionState.class,
                new Key<DumpActionState>(DumpActionState.class, objectId));
    }

}
