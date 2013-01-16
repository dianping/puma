package com.dianping.puma.admin.service.impl;

import java.util.Date;

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
    public Long create(DumpAction dumpAction) {
        Key<DumpAction> key = this.dumpActionDao.save(dumpAction);
        this.dumpActionDao.getDatastore().ensureIndexes();
        Long id = (Long) key.getId();
        //创建DumpActionState
        DumpActionState state = new DumpActionState();
        state.setId(id);
        state.setState(State.PREPARABLE);
        state.setDetail(State.PREPARABLE.getDesc());
        Date curDate = new Date();
        state.setCreateTime(curDate);
        state.setLastUpdateTime(curDate);
        BinlogInfo binlogBin = new BinlogInfo();
        binlogBin.setBinlogFile("");
        binlogBin.setBinlogPosition(-1);
        state.setBinlogInfo(binlogBin);
        dumpActionStateService.save(state);

        return id;
    }

    @Override
    public DumpAction find(Long id) {
        return this.dumpActionDao.getDatastore().getByKey(DumpAction.class, new Key<DumpAction>(DumpAction.class, id));
    }

}
