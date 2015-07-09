package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.biz.entity.old.CatchupTask;
import com.dianping.puma.biz.entity.old.DstDBInstance;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.executor.exception.TEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatchupTaskExecutor extends AbstractTaskExecutor<CatchupTask> {
    protected static final Logger LOG = LoggerFactory.getLogger(CatchupTaskExecutor.class);
    /** 追赶的SyncTaskExecutor */
    private SyncTaskExecutor syncTaskExecutor;

    private int threshold = 500;

    //    /** 追赶的binlog的终点 */
    //    private BinlogInfo binlogInfoEnd;

    public CatchupTaskExecutor(CatchupTask catchupTask, String pumaServerHost, int pumaServerPort, String target,
                               SyncTaskExecutor syncTaskExecutor, DstDBInstance dstDBInstance) {
        //super(catchupTask, pumaServerHost, pumaServerPort, target, dstDBInstance);
        this.syncTaskExecutor = syncTaskExecutor;
    }

    @Override
    public void init() {}

    @Override
    public void destroy() {}

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    protected void doStart() {}

    @Override
    protected void doStop() {}

    @Override
    protected void execute(ChangedEvent event) throws TEException {
        //执行同步
        //mysqlExecutor.execute(event);
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
