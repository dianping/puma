package com.dianping.puma.admin.service.impl;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.DumpActionService;
import com.dianping.puma.admin.service.DumpActionStateService;
import com.dianping.puma.core.sync.dao.action.DumpActionDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.action.ActionState.State;
import com.dianping.puma.core.sync.model.action.DumpAction;
import com.dianping.puma.core.sync.model.action.DumpActionState;
import com.google.code.morphia.Key;

@Service("dumpActionService")
public class DumpActionServiceImpl implements DumpActionService {
    @Autowired
    DumpActionDao dumpActionDao;
    @Autowired
    DumpActionStateService dumpActionStateService;

    @Override
    public ObjectId create(DumpAction dumpAction) {
        Key<DumpAction> key = this.dumpActionDao.save(dumpAction);
        this.dumpActionDao.getDatastore().ensureIndexes();
        ObjectId id = (ObjectId) key.getId();
        //创建DumpActionState
        DumpActionState state = new DumpActionState();
        state.setId(id);
        state.setState(State.CREATED);
        state.setDetail("创建。");
        state.setCreateTime(new Date());
        BinlogInfo binlogBin = new BinlogInfo();
        binlogBin.setBinlogFile("");
        binlogBin.setBinlogPosition(-1);
        state.setBinlogInfo(binlogBin);
        dumpActionStateService.save(state);

        return id;
    }

    @Override
    public DumpAction find(ObjectId objectId) {
        return this.dumpActionDao.getDatastore().getByKey(DumpAction.class, new Key<DumpAction>(DumpAction.class, objectId));
    }

}
