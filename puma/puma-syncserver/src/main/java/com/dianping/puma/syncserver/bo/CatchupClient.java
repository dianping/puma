package com.dianping.puma.syncserver.bo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.model.BinlogInfo;

public class CatchupClient extends AbstractSyncClient {
    private static final Logger LOG = LoggerFactory.getLogger(CatchupClient.class);

    /** 追赶的binlog的终点 */
    private BinlogInfo binlogInfoEnd;

    public CatchupClient(SyncConfig sync, BinlogInfo startedBinlogInfo, BinlogInfo binlogTo) {
        super(sync, startedBinlogInfo);
        this.binlogInfoEnd = binlogTo;
        LOG.info("CatchupClient inited.");
    }

    @Override
    protected void onEvent(ChangedEvent event) throws Exception {
        //判断是否已经到达binlogTo
        if (StringUtils.equals(binlogInfoEnd.getBinlogFile(), curBinlogInfo.getBinlogFile())
                && binlogInfoEnd.getBinlogPosition() <= curBinlogInfo.getBinlogPosition()) {
            stop();
            return;
        }
        //执行同步
        mysqlExecutor.execute(event);
    }

}
