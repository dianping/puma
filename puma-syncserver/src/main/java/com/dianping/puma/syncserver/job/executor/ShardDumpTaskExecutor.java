package com.dianping.puma.syncserver.job.executor;

import com.dianping.cat.Cat;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.state.ShardDumpTaskState;
import com.dianping.puma.core.service.ShardDumpTaskService;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.util.ProcessBuilderWrapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDumpTaskExecutor implements TaskExecutor<ShardDumpTask, ShardDumpTaskState> {
    private static final Logger logger = LoggerFactory.getLogger(ShardDumpTaskExecutor.class);

    private static final Charset DEFAULT_CJARSET = Charset.forName("utf8");

    private static final Pattern BINLOG_PATTERN = Pattern.compile(".*MASTER_LOG_FILE='([^']+)', MASTER_LOG_POS=(\\d+).*");

    protected final ShardDumpTask task;

    protected final ShardDumpTaskState dumpStatus;

    protected final ShardDumpTaskState loadStatus;

    protected final String dumpOutputDir;

    protected volatile SrcDBInstance srcDBInstance;

    protected volatile DstDBInstance dstDBInstance;

    protected final BlockingQueue<Long> waitForLoadQueue = new LinkedBlockingQueue<Long>(5);

    protected Thread dumpWorker;

    protected Thread loadWorker;

    private ShardDumpTaskService shardDumpTaskService;

    public ShardDumpTaskExecutor(ShardDumpTask task) {
        checkNotNull(task, "task");
        checkNotNull(task.getTableName(), "task.tableName");
        checkNotNull(task.getIndexColumnName(), "task.indexColumnName");

        this.task = task;
        this.dumpOutputDir = (SyncServerConfig.getInstance() == null ? "/tmp/" : SyncServerConfig.getInstance().getTempDir() + "/dump/") + task.getName() + "/";
        this.dumpStatus = new ShardDumpTaskState();
        this.dumpStatus.setStatus(Status.PREPARING);
        this.loadStatus = new ShardDumpTaskState();
        this.loadStatus.setStatus(Status.PREPARING);
    }

    public void init() {
        this.dumpWorker = new Thread(new DumpWorker());
        this.loadWorker = new Thread(new LoadWorker());
        createOutPutDir();
    }

    protected synchronized void saveTask() {
        shardDumpTaskService.update(this.task);
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

        protected boolean checkHasData(long index) {
            FileInputStream reader = null;
            boolean hasData = false;
            File file = null;
            try {
                file = new File(getDumpFile(index));
                if (file.length() >= 1024 * 8) {
                    hasData = true;
                    return hasData;
                }
                hasData = FileUtils.fileRead(file).contains("INSERT");
                return hasData;
            } catch (IOException e) {
                hasData = false;
                return hasData;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignoire) {
                    }
                }
                if (!hasData && file != null) {
                    file.delete();
                }
            }
        }

        @Override
        public void run() {
            dumpStatus.setStatus(Status.RUNNING);

            while (true) {
                long nextIndex = increaseIndex();

                try {
                    String output = mysqldump(this.lastIndex, nextIndex);
                    if (!Strings.isNullOrEmpty(output)) {
                        throw new IOException(output);
                    }

                    if (!checkHasData(this.lastIndex)) {
                        dumpStatus.setPercent(ShardDumpTaskState.PERCENT_MAX);
                        waitForLoadQueue.put(-1l);
                        break;
                    }

                    output = convertTableName(this.lastIndex);
                    if (!Strings.isNullOrEmpty(output)) {
                        throw new IOException(output);
                    }

                    checkAndUpdateBinlogInfo(this.lastIndex);

                    dumpStatus.setPercent((int) (this.lastIndex * 100 / task.getMaxKey()));
                    waitForLoadQueue.put(lastIndex);
                    this.lastIndex = nextIndex;

                } catch (InterruptedException e) {
                    dumpStatus.setStatus(Status.SUSPENDED);
                    break;
                } catch (Exception e) {
                    String msg = "Dump Failed!";
                    logger.error(msg, e);
                    Cat.logError(msg, e);
                    dumpStatus.setStatus(Status.FAILED);
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

        protected void checkAndUpdateBinlogInfo(long index) throws IOException {
            //-- CHANGE MASTER TO MASTER_LOG_FILE='mysqlbin-log.000010', MASTER_LOG_POS=91841517;
            String firstLine = readFirstLine(index);

            Matcher result = BINLOG_PATTERN.matcher(firstLine);
            if (!result.matches() || result.groupCount() != 2) {
                throw new IOException("BINLOG read failed! " + firstLine);
            }

            String binlog = result.group(1);
            long position = Long.parseLong(result.group(2));

            checkAndUpdateBinlogInfo(binlog, position);
        }

        protected void checkAndUpdateBinlogInfo(String binlog, long position) throws IOException {
            int oldBinlogIndex = getBinlogIndex(task.getBinlogInfo().getBinlogFile());
            int newBinlogIndex = getBinlogIndex(binlog);

            if (newBinlogIndex < oldBinlogIndex ||
                    (newBinlogIndex == oldBinlogIndex && position < task.getBinlogInfo().getBinlogPosition())) {
                BinlogInfo info = new BinlogInfo();
                info.setBinlogFile(binlog);
                info.setBinlogPosition(position);
                task.setBinlogInfo(info);
                saveTask();
            }
        }

        protected int getBinlogIndex(String binlog) throws IOException {
            //mysql-bin.0000001
            int index = binlog.lastIndexOf(".");
            if (index < 0 || index == binlog.length() - 1) {
                throw new IOException("binlog file name error: " + binlog);
            }
            return Integer.parseInt(binlog.substring(index + 1));
        }

        protected String readFirstLine(long index) throws IOException {
            return Files.readFirstLine(new File(getDumpFile(index)), DEFAULT_CJARSET);
        }
    }

    class LoadWorker implements Runnable {
        protected void cleanUp(long index) {
            new File(getDumpFile(index)).delete();
            task.setIndexKey(index);
            saveTask();
        }

        @Override
        public void run() {
            loadStatus.setStatus(Status.RUNNING);

            while (true) {
                try {
                    Long index = waitForLoadQueue.take();
                    if (index < 0) {
                        cleanup();
                        loadStatus.setPercent(ShardDumpTaskState.PERCENT_MAX);
                        break;
                    }

                    String output = mysqlload(index);
                    if (!Strings.isNullOrEmpty(output)) {
                        throw new IOException(output);
                    }
                    cleanUp(index);
                    loadStatus.setPercent((int) (index * 100 / task.getMaxKey()));
                } catch (InterruptedException e) {
                    loadStatus.setStatus(Status.SUSPENDED);
                    break;
                } catch (Exception e) {
                    String msg = "Convert Failed!";
                    logger.error(msg, e);
                    Cat.logError(msg, e);
                    loadStatus.setStatus(Status.FAILED);
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
        loadWorker.start();
    }

    @Override
    public void pause(String detail) {
        stop(detail);
    }

    @Override
    public void succeed() {

    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return null;
    }

    @Override
    public ShardDumpTask getTask() {
        return this.task;
    }

    @Override
    public ShardDumpTaskState getTaskState() {
        ShardDumpTaskState status = new ShardDumpTaskState();
        if (!dumpStatus.getStatus().equals(Status.RUNNING)) {
            status.setStatus(dumpStatus.getStatus());
        } else if (!loadStatus.getStatus().equals(Status.RUNNING)) {
            status.setStatus(loadStatus.getStatus());
        } else {
            status.setStatus(Status.RUNNING);
        }

        status.setTaskName(this.task.getName());
        status.setPercent((dumpStatus.getPercent() + loadStatus.getPercent()) / 2);
        return status;
    }

    @Override
    public void setTaskState(ShardDumpTaskState taskState) {

    }

    @Override
    public void stop(String detail) {
        dumpWorker.interrupt();
        loadWorker.interrupt();

        while (!dumpWorker.isInterrupted() || loadWorker.isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        cleanup();
    }

    protected void cleanup() {
        try {
            FileUtils.deleteDirectory(dumpOutputDir);
        } catch (IOException e) {
        }
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
