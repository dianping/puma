package com.dianping.puma.syncserver.job.executor;

import com.dianping.cat.Cat;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.service.ShardDumpTaskService;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.util.ProcessBuilderWrapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDumpTaskExecutor implements TaskExecutor<ShardDumpTask, TaskState> {
    private static final Logger logger = LoggerFactory.getLogger(ShardDumpTaskExecutor.class);

    protected final ShardDumpTask task;

    protected final TaskExecutorStatus status;

    protected final String dumpOutputDir;

    protected volatile SrcDBInstance srcDBInstance;

    protected volatile DstDBInstance dstDBInstance;

    protected final BlockingQueue<Long> waitForConvertQueue = new LinkedBlockingQueue<Long>(5);

    protected final BlockingQueue<Long> waitForLoadQueue = new LinkedBlockingQueue<Long>(5);

    protected Thread dumpWorker;

    protected Thread convertWorker;

    protected Thread loadWorker;

    private ShardDumpTaskService shardDumpTaskService;

    public ShardDumpTaskExecutor(ShardDumpTask task) {
        checkNotNull(task, "task");
        checkNotNull(task.getTableName(), "task.tableName");
        checkNotNull(task.getIndexColumnName(), "task.indexColumnName");

        this.task = task;
        this.dumpOutputDir = (SyncServerConfig.getInstance() == null ? "/tmp/" : SyncServerConfig.getInstance().getTempDir() + "/dump/") + task.getName() + "/";
        this.status = new TaskExecutorStatus();
    }

    public void init() {
        this.dumpWorker = new Thread(new DumpWorker());
        this.convertWorker = new Thread(new ConvertWorker());
        this.loadWorker = new Thread(new LoadWorker());
        createOutPutDir();
    }

    protected void createOutPutDir() {
        File theDir = new File(this.dumpOutputDir);
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }

    protected String getDumpFile(long index) {
        return String.format("%s%d-%d.dump.sql", dumpOutputDir, task.getShardRule().hashCode(), index);
    }

    class DumpWorker implements Runnable {
        protected long lastIndex;

        public DumpWorker() {
            this.lastIndex = task.getIndexKey();
        }

        protected boolean checkHasRow(long index) {
            File file = new File(getDumpFile(index));
            boolean hasRow = file.exists() && file.length() > 0;
            if (!hasRow) {
                file.delete();
            }
            return hasRow;
        }

        @Override
        public void run() {
            while (true) {
                long nextIndex = increaseIndex();

                try {
                    String output = mysqldump(this.lastIndex, nextIndex);
                    if (!Strings.isNullOrEmpty(output)) {
                        throw new IOException(output);
                    }

                    if (!checkHasRow(this.lastIndex)) {
                        //todo: finish!
                        break;
                    }

                    waitForConvertQueue.put(lastIndex);
                    this.lastIndex = nextIndex;

                } catch (InterruptedException e) {
                    status.setStatus(TaskExecutorStatus.Status.SUSPPENDED);
                    break;
                } catch (Exception e) {
                    String msg = "Dump Failed!";
                    logger.error(msg, e);
                    Cat.logError(msg, e);
                    status.setStatus(TaskExecutorStatus.Status.FAILED);
                    break;
                }
            }
        }

        protected long increaseIndex() {
            return this.lastIndex + 1000000;
        }

        protected String mysqldump(long lastIndex, long nextIndex) throws IOException, InterruptedException {
            checkNotNull(srcDBInstance, "srcDBInstance");

            List<String> cmdlist = new ArrayList<String>();

            cmdlist.add("mysqldump");
            cmdlist.add("--host=" + srcDBInstance.getHost());
            cmdlist.add("--port=" + srcDBInstance.getPort());
            cmdlist.add("--user=" + srcDBInstance.getUsername());
            cmdlist.add("--password=" + srcDBInstance.getPassword());
            cmdlist.add("--where=" + String.format("%s AND %s > %d AND %s <= %d AND %s <= %d",
                    task.getShardRule(),
                    task.getIndexColumnName(), lastIndex,
                    task.getIndexColumnName(), nextIndex,
                    task.getIndexColumnName(), task.getMaxKey()));

            cmdlist.addAll(task.getOptions());
            cmdlist.add("--result-file=" + getDumpFile(lastIndex));
            cmdlist.add(task.getDataBase());
            cmdlist.add(task.getTableName());

            return executeByProcessBuilder(cmdlist);
        }
    }

    class ConvertWorker implements Runnable {

        protected String convertTableName(long index) throws IOException, InterruptedException {
            List<String> cmdlist = new ArrayList<String>();
            cmdlist.add("perl");
            cmdlist.add("-i");
            cmdlist.add("-p");
            cmdlist.add("-e");
            cmdlist.add("s/(^INSERT )(INTO )(`)([^`]+)(`)(.*$)/\\1IGNORE \\2\\3" + task.getTargetTableName() + "\\5\\6/g");
            cmdlist.add(getDumpFile(index));
            return executeByProcessBuilder(cmdlist);
        }

        protected void readBinLogPostion(long index) {
            //todo:load
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Long index = waitForConvertQueue.take();
                    String output = convertTableName(index);
                    if (!Strings.isNullOrEmpty(output)) {
                        throw new IOException(output);
                    }
                    waitForLoadQueue.put(index);
                } catch (InterruptedException e) {
                    status.setStatus(TaskExecutorStatus.Status.SUSPPENDED);
                    break;
                } catch (Exception e) {
                    String msg = "Convert Failed!";
                    logger.error(msg, e);
                    Cat.logError(msg, e);
                    status.setStatus(TaskExecutorStatus.Status.FAILED);
                    break;
                }
            }
        }
    }

    class LoadWorker implements Runnable {

        protected void cleanUp(long index) {
            new File(getDumpFile(index)).delete();
            //todo: 回写数据库
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Long index = waitForLoadQueue.take();
                    String output = mysqlload(index);
                    if (!Strings.isNullOrEmpty(output)) {
                        throw new IOException(output);
                    }
                    cleanUp(index);
                } catch (InterruptedException e) {
                    status.setStatus(TaskExecutorStatus.Status.SUSPPENDED);
                    break;
                } catch (Exception e) {
                    String msg = "Convert Failed!";
                    logger.error(msg, e);
                    Cat.logError(msg, e);
                    status.setStatus(TaskExecutorStatus.Status.FAILED);
                    break;
                }
            }
        }

        protected String mysqlload(long index) throws IOException, InterruptedException {
            checkNotNull(dstDBInstance, "dstDBInstance");

            List<String> cmdlist = new ArrayList<String>();
            cmdlist.add("mysql -f --default-character-set=utf8");
            cmdlist.add("'--database=" + task.getDataBase() + "'");
            cmdlist.add("'--user=" + dstDBInstance.getUsername() + "'");
            cmdlist.add("'--host=" + dstDBInstance.getHost() + "'");
            cmdlist.add("'--port=" + dstDBInstance.getPort() + "'");
            cmdlist.add("'--password=" + dstDBInstance.getPassword() + "'");
            cmdlist.add("< '" + getDumpFile(index) + "'");

            return executeByProcessBuilder(Lists.newArrayList("sh", "-c", StringUtils.join(cmdlist, " ")));
        }

    }

    protected String executeByProcessBuilder(List<String> cmd) throws IOException, InterruptedException {
        logger.info("execute shell script, cmd is: " + StringUtils.join(cmd, ' '));
        ProcessBuilderWrapper pbd = new ProcessBuilderWrapper(cmd);
        logger.info("Command has terminated with status: " + pbd.getStatus());
        logger.info("Output:\n" + pbd.getInfos());
        return pbd.getErrors();
    }

    @Override
    public void start() {
        dumpWorker.start();
        convertWorker.start();
        loadWorker.start();
    }

    @Override
    public void pause(String detail) {

    }

    @Override
    public void succeed() {

    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return this.status;
    }

    @Override
    public ShardDumpTask getTask() {
        return null;
    }

    @Override
    public TaskState getTaskState() {
        return null;
    }

    @Override
    public void setTaskState(TaskState taskState) {

    }

    @Override
    public void stop(String detail) {

    }

    public void setSrcDBInstance(SrcDBInstance srcDBInstance) {
        this.srcDBInstance = srcDBInstance;
    }

    public void setDstDBInstance(DstDBInstance dstDBInstance) {
        this.dstDBInstance = dstDBInstance;
    }

    public void setShardDumpTaskService(ShardDumpTaskService shardDumpTaskService) {
        this.shardDumpTaskService = shardDumpTaskService;
    }
}
