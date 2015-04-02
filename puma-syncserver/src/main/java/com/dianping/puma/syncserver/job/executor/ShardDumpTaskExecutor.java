package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.util.ProcessBuilderWrapper;
import com.google.common.collect.Lists;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public ShardDumpTaskExecutor(ShardDumpTask task) {
        checkNotNull(task, "task");
        checkNotNull(task.getTableName(), "task.tableName");

        this.task = task;
        this.dumpOutputDir = SyncServerConfig.getInstance() == null ? "/tmp/" : SyncServerConfig.getInstance().getTempDir() + "/dump/" + task.getName() + "/";
        this.status = new TaskExecutorStatus();
    }

    public void init() {
    }


    protected String mysqldump() throws IOException, InterruptedException {
        checkNotNull(srcDBInstance, "srcDBInstance");

        List<String> cmdlist = new ArrayList<String>();

        cmdlist.add("mysqldump");
        cmdlist.add("--host=" + srcDBInstance.getHost());
        cmdlist.add("--port=" + srcDBInstance.getPort());
        cmdlist.add("--user=" + srcDBInstance.getUsername());
        cmdlist.add("--password=" + srcDBInstance.getPassword());
        cmdlist.add("--where=" + task.getShardRule());
        cmdlist.addAll(task.getOptions());
        cmdlist.add("--result-file=" + getDumpFile());
        cmdlist.add(task.getDataBase());
        cmdlist.add(task.getTableName());

        String output = executeByProcessBuilder(cmdlist);
        return output;
    }

    protected String getDumpFile() {
        return String.format("%s%s/%d.dump.sql", dumpOutputDir, task.getName(), task.getShardRule().hashCode());
    }


    private String mysqlload() throws ExecuteException, IOException, InterruptedException {
        checkNotNull(dstDBInstance, "dstDBInstance");

        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add("mysql -f --default-character-set=utf8");
        cmdlist.add("'--database=" + task.getDataBase() + "'");
        cmdlist.add("'--user=" + dstDBInstance.getUsername() + "'");
        cmdlist.add("'--host=" + dstDBInstance.getHost() + "'");
        cmdlist.add("'--port=" + dstDBInstance.getPort() + "'");
        cmdlist.add("'--password=" + dstDBInstance.getPassword() + "'");
        cmdlist.add("< '" + getDumpFile() + "'");

        return executeByProcessBuilder(Lists.newArrayList("sh", "-c", StringUtils.join(cmdlist, " ")));
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

    public SrcDBInstance getSrcDBInstance() {
        return srcDBInstance;
    }

    public void setSrcDBInstance(SrcDBInstance srcDBInstance) {
        this.srcDBInstance = srcDBInstance;
    }

    public DstDBInstance getDstDBInstance() {
        return dstDBInstance;
    }

    public void setDstDBInstance(DstDBInstance dstDBInstance) {
        this.dstDBInstance = dstDBInstance;
    }
}
