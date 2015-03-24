package com.dianping.puma.syncserver.job.executor;

import java.sql.SQLException;

import com.dianping.puma.core.entity.CatchupTask;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.state.CatchupTaskState;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.executor.exception.DdlRenameException;

public class CatchupTaskExecutor extends AbstractTaskExecutor<CatchupTask, CatchupTaskState> {
    protected static final Logger LOG = LoggerFactory.getLogger(CatchupTaskExecutor.class);
    /** 追赶的SyncTaskExecutor */
    private SyncTaskExecutor syncTaskExecutor;

    private int threshold = 500;

    //    /** 追赶的binlog的终点 */
    //    private BinlogInfo binlogInfoEnd;

    public CatchupTaskExecutor(CatchupTask catchupTask, CatchupTaskState catchupTaskState, String pumaServerHost, int pumaServerPort, String target,
                               SyncTaskExecutor syncTaskExecutor, DstDBInstance dstDBInstance) {
        super(catchupTask, catchupTaskState, pumaServerHost, pumaServerPort, target, dstDBInstance);
        this.syncTaskExecutor = syncTaskExecutor;
    }

    @Override
    protected void execute(ChangedEvent event) throws SQLException, DdlRenameException {
        //执行同步
        mysqlExecutor.execute(event);
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void binlogOfIOThreadChanged(ChangedEvent event) {
        super.binlogOfIOThreadChanged(event);
        BinlogInfo catchupBinlogInfo = this.status.getBinlogInfoOfIOThread();
        BinlogInfo syncBinlogInfo = this.syncTaskExecutor.getTaskExecutorStatus().getBinlogInfoOfIOThread();
        if (syncBinlogInfo == null || catchupBinlogInfo == null) {
            return;
        }
        //(1) 如果CatchupExcutor比SyncTaskExecutor慢且很接近(或者相等)
        if (StringUtils.equals(syncBinlogInfo.getBinlogFile(), catchupBinlogInfo.getBinlogFile())
                && syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition() >= 0
                && syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition() < threshold) {
            //停止SyncTaskExecutor
            this.syncTaskExecutor.pause("Auto paused because CatchupExcutor is close to SyncTaskExecutor.");
            //因为syncTaskExecutor的执行是在另外的线程中，故stop后再次获取syncTaskExecutor的binlogInfo才是真正的binlogInfo
            syncBinlogInfo = this.syncTaskExecutor.getTaskExecutorStatus().getBinlogInfoOfIOThread();
            //此时再判断，如果需要catchupTaskExecutor可以追赶一下，如果不需要，则停止并记录状态
            long diffPos = syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition();
            if (diffPos == 0) {
                this.succeed();
                syncTaskExecutor.succeed();
                LOG.info("TaskExecutor[" + this.getTask().getPumaClientName()
                        + "] catch up succeeded with catchupBinlogInfo(io thread):" + catchupBinlogInfo
                        + ", syncBinlogInfo(io thread):" + syncBinlogInfo);
            }
        } else if (StringUtils.equals(syncBinlogInfo.getBinlogFile(), catchupBinlogInfo.getBinlogFile())
                && syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition() < 0) {
            //(2) 如果CatchupExcutor比SyncTaskExecutor快，则放慢CatchupExcutor,恢复SyncTaskExecutor
            LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] catchupBinlogInfo(io thread):" + catchupBinlogInfo
                    + ", syncBinlogInfo(io thread):" + syncBinlogInfo);
            this.speedDown();
            this.syncTaskExecutor.resetSpeed();
        } else if (StringUtils.equals(syncBinlogInfo.getBinlogFile(), catchupBinlogInfo.getBinlogFile())
                && syncBinlogInfo.getBinlogPosition() - catchupBinlogInfo.getBinlogPosition() > 0) {
            //(3) 如果CatchupExcutor比SyncTaskExecutor慢，则放慢SyncTaskExecutor,恢复CatchupExcutor
            LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] catchupBinlogInfo(io thread):" + catchupBinlogInfo
                    + ", syncBinlogInfo(io thread):" + syncBinlogInfo);
            this.resetSpeed();
            this.syncTaskExecutor.speedDown();

        }
    }

}
