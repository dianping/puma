package com.dianping.puma.syncserver.job.executor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.CatchupTask;

public class CatchupTaskExecutor extends AbstractTaskExecutor<CatchupTask> {
    protected static final Logger LOG = LoggerFactory.getLogger(CatchupTaskExecutor.class);
    /** 追赶的SyncTaskExecutor */
    private SyncTaskExecutor syncTaskExecutor;

    private int threshold = 50;

    /** 追赶的binlog的终点 */
    //    private BinlogInfo binlogInfoEnd;

    public CatchupTaskExecutor(CatchupTask catchupTask, String pumaServerHost, int pumaServerPort, String target,
                               SyncTaskExecutor syncTaskExecutor) {
        super(catchupTask, pumaServerHost, pumaServerPort, target);
        this.syncTaskExecutor = syncTaskExecutor;
    }

    @Override
    protected void onEvent(ChangedEvent event) throws Exception {
        //执行同步
        mysqlExecutor.execute(event);
        BinlogInfo catchupBinlogInfo = this.getTask().getBinlogInfo();
        BinlogInfo syncBinlogInfo = this.syncTaskExecutor.getTask().getBinlogInfo();
        //(1) 如果CatchupExcutor比SyncTaskExecutor慢且很接近(或者相等)
        if (StringUtils.equals(syncBinlogInfo.getBinlogFile(), catchupBinlogInfo.getBinlogFile())
                && syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition() >= 0
                && syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition() < threshold) {
            //停止SyncTaskExecutor
            this.syncTaskExecutor.pause("Auto paused because CatchupExcutor is close to SyncTaskExecutor.");
            //因为syncTaskExecutor的执行是在另外的线程中，故stop后再次获取syncTaskExecutor的binlogInfo才是真正的binlogInfo
            syncBinlogInfo = this.syncTaskExecutor.getTask().getBinlogInfo();
            //此时再判断，如果需要catchupTaskExecutor可以追赶一下，如果不需要，则停止并记录状态
            long diffPos = syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition();
            if (diffPos == 0) {
                this.succeed();
                syncTaskExecutor.succeed();
            }
        } else if (StringUtils.equals(syncBinlogInfo.getBinlogFile(), catchupBinlogInfo.getBinlogFile())
                && syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition() < 0) {
            //(2) 如果CatchupExcutor比SyncTaskExecutor快，则放慢CatchupExcutor
            this.speedDown();
        }
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

}
