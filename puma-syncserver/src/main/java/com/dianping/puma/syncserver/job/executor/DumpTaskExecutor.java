package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.biz.entity.old.DstDBInstance;
import com.dianping.puma.biz.entity.old.DumpTask;
import com.dianping.puma.biz.entity.old.SrcDBInstance;
import com.dianping.puma.biz.entity.TaskState;
import com.dianping.puma.biz.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.biz.sync.model.mapping.TableMapping;
import com.dianping.puma.biz.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.job.executor.exception.TEException;
import com.dianping.puma.syncserver.util.ProcessBuilderWrapper;
import com.google.common.base.Strings;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wukezhu
 */
public class DumpTaskExecutor implements TaskExecutor<DumpTask> {
    private static final Logger LOG = LoggerFactory.getLogger(DumpTaskExecutor.class);
    private final static Pattern BINLOG_LINE_PATTERN = Pattern.compile("^.+LOG_FILE='(.*)',\\s+.+LOG_POS=([0-9]+);$");
    protected static final String CHARSET = "iso-8859-1";

    private final String uuid;
    private final String dumpOutputDir;
    private Thread thread;
    private Timer timer = new Timer();

    private SrcDBInstance srcDBInstance;

    private DstDBInstance dstDBInstance;

    public DstDBInstance getDstDBInstance() {
        return dstDBInstance;
    }

    public void setDstDBInstance(DstDBInstance dstDBInstance) {
        this.dstDBInstance = dstDBInstance;
    }

    public SrcDBInstance getSrcDBInstance() {
        return srcDBInstance;
    }

    public void setSrcDBInstance(SrcDBInstance srcDBInstance) {
        this.srcDBInstance = srcDBInstance;
    }

    private Executor executor = new DefaultExecutor();

    {
        executor.setExitValue(1);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
        executor.setWatchdog(watchdog);
    }

    private DumpTask dumpTask;

    protected TaskState state;

    private Process proc;
    protected TaskExecutorStatus status;

    public DumpTaskExecutor(DumpTask dumpTask, TaskState dumpTaskState) throws IOException {
        this.uuid = UUID.randomUUID().toString();
        this.dumpOutputDir = SyncServerConfig.getInstance().getTempDir() + "/dump/" + uuid + "/";
        FileUtils.forceMkdir(new File(dumpOutputDir));
        this.dumpTask = dumpTask;
        state = dumpTaskState;
        state.setName(dumpTask.getName());
        state.setServerName(SyncServerConfig.getInstance().getSyncServerName());

        //this.status = new TaskExecutorStatus();
        //status.setTaskName(dumpTask.getName());
        //status.setSyncType(dumpTask.getSyncType());
        //status.setTaskId(dumpTask.getId());
        //status.setType(dumpTask.getType());
    }

    @Override
    public void init() {

    }

    /**
     * 根据dumpConfig，进行dump，并返回binlog位置<br>
     */
    @Override
    public void start() {
        thread = new Thread() {
            public void run() {
                LOG.info("started dump.");
                state.setStatus(Status.DUMPING);
                //status.setStatus(TaskExecutorStatus.Status.DUMPING);
                try {
                    //(1) dump
                    //目前dump的数据库配置只允许一个
                    List<DatabaseMapping> databaseMappings = dumpTask.getDumpMapping().getDatabaseMappings();
                    DatabaseMapping databaseMapping = databaseMappings.get(0);
                    //执行dump脚本，dump到<dump_tempDir>/<uuid>目录
                    String srcDatabaseName = databaseMapping.getFrom();
                    String destDatabaseName = databaseMapping.getTo();
                    List<TableMapping> tableMappings = databaseMapping.getTables();
                    List<String> srcTableNames = getSrcTableNames(tableMappings);
                    List<String> destTableNames = getDestTableNames(tableMappings);
                    String output = _mysqldump(srcDatabaseName, srcTableNames);
                    if (hasException(output)) {
                        throw new DumpException("mysqldump output is not empty , so consided to be failed: " + output);
                    }
                    LOG.info("dump done.");
                    LineIterator lineIterators = IOUtils.lineIterator(new FileInputStream(_getDumpFile(srcDatabaseName)), CHARSET);
                    PrintWriter deelFileWriter = new PrintWriter(new File(_getSourceFile(srcDatabaseName)), CHARSET);
                    deelFileWriter.println("CREATE DATABASE IF NOT EXISTS " + destDatabaseName + ";USE " + destDatabaseName + ";");//添加select database语句
                    while (lineIterators.hasNext()) {
                        String line = lineIterators.next();
                        //获取binlog位置
                        if (state.getBinlogInfo() == null) {
                            Matcher matcher = BINLOG_LINE_PATTERN.matcher(line);
                            if (matcher.matches()) {
                                BinlogInfo binlogInfo = new BinlogInfo();
                                binlogInfo.setBinlogFile(matcher.group(1));
                                binlogInfo.setBinlogPosition(Long.parseLong(matcher.group(2)));
                                state.setBinlogInfo(binlogInfo);
                            }
                        }
                        //table更名
                        for (int i = 0; i < srcTableNames.size(); i++) {
                            String srcTableName = srcTableNames.get(i);
                            String destTableName = destTableNames.get(i);
                            if (!StringUtils.equalsIgnoreCase(srcTableName, destTableName)) {
                                String originLine = line;
                                line = line.replace("INSERT INTO `" + srcTableName + "`", "INSERT INTO `" + destTableName + "`");
                                line = line.replace("table `" + srcTableName + "`", "table `" + destTableName + "`");
                                line = line.replace("TABLE `" + srcTableName + "`", "TABLE `" + destTableName + "`");
                                if (!StringUtils.equals(line, originLine)) {//line已经改变，即table被替换过，则不需要再查找
                                    break;
                                }
                            }
                        }
                        //(不自动创建table) 替换 CREATE TABLE 为 CREATE TABLE IF NOT EXISTS
                        //                        if (StringUtils.startsWith(line, "CREATE TABLE")) {
                        //                            line = line.replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS");
                        //                        }

                        deelFileWriter.println(line);
                    }
                    deelFileWriter.close();
                    if (state.getBinlogInfo() == null || StringUtils.isBlank(state.getBinlogInfo().getBinlogFile())
                            || state.getBinlogInfo().getBinlogPosition() <= 0) {
                        throw new DumpException("binlogFile or binlogPos is Error: binlogFile="
                                + state.getBinlogInfo().getBinlogFile() + ",binlogPos="
                                + state.getBinlogInfo().getBinlogPosition());
                    }
                    LOG.info("binlog info:" + state.getBinlogInfo());
                    //(2) load
                    state.setStatus(Status.LOADING);
                    LOG.info("started load.");
                    //执行load脚本
                    srcDatabaseName = databaseMapping.getFrom();
                    output = _mysqlload(srcDatabaseName);
                    if (hasException(output)) {
                        throw new DumpException("mysqlload output is not empty , so consided to be failed: " + output);
                    }
                    state.setStatus(Status.SUCCESS);
                    LOG.info("load done.");
                } catch (Exception e) {
                    fail(e.getMessage());
                    LOG.error("dump error: " + e.getMessage(), e);
                }
            }
        };
        thread.start();
        state.setStatus(Status.RUNNING);
    }

    @Override
    public void destroy() {

    }

    public TEException exception() {
        return new TEException(0);
    }

    /**
     * 从TableConfig中获取同步源的table名称
     */
    private List<String> getSrcTableNames(List<TableMapping> tableConfigs) {
        List<String> srcTableNames = new ArrayList<String>();
        if (tableConfigs != null && tableConfigs.size() > 0) {
            for (TableMapping tableConfig : tableConfigs) {
                srcTableNames.add(tableConfig.getFrom());
            }
        }
        return srcTableNames;
    }

    /**
     * 从TableConfig中获取同步目标的table名称
     */
    private List<String> getDestTableNames(List<TableMapping> tableConfigs) {
        List<String> destTableNames = new ArrayList<String>();
        if (tableConfigs != null && tableConfigs.size() > 0) {
            for (TableMapping tableConfig : tableConfigs) {
                destTableNames.add(tableConfig.getTo());
            }
        }
        return destTableNames;
    }

    private boolean hasException(String output) {
        if (Strings.isNullOrEmpty(output)) {
            return false;
        }
        String[] lines = output.split("\r\n|\r|\n");
        for (String line : lines) {
            if (Strings.isNullOrEmpty(line)) {
                continue;
            }
            if (line.startsWith("Warning:")) {
                continue;
            }
            return true;
        }
        return false;
    }

    private String _getDumpFile(String databaseName) {
        return dumpOutputDir + databaseName + ".dump.sql";
    }

    private String _getSourceFile(String databaseName) {
        return dumpOutputDir + databaseName + ".source.sql";
    }

    private String _mysqldump(String databaseName, List<String> tableNames) throws IOException, InterruptedException {
        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add("mysqldump");
        //String hostWithPort = dumpTask.getSrcMysqlHost().getHost();
        //String host = hostWithPort;
        String host = srcDBInstance.getHost();
        //int port = 3306
        int port = srcDBInstance.getPort();
        /*if (StringUtils.contains(hostWithPort, ':')) {
            String[] splits = hostWithPort.split(":");
            host = splits[0];
            port = Integer.parseInt(splits[1]);
        }*/
        cmdlist.add("--host=" + host);
        cmdlist.add("--port=" + port);
        //cmdlist.add("--user=" + dumpTask.getSrcMysqlHost().getUsername());
        //cmdlist.add("--password=" + dumpTask.getSrcMysqlHost().getPassword());
        cmdlist.add("--user=" + srcDBInstance.getUsername());
        cmdlist.add("--password=" + srcDBInstance.getPassword());
        for (String opt : dumpTask.getOptions()) {
            cmdlist.add(opt);
        }
        String outputFileName = _getDumpFile(databaseName);
        cmdlist.add("--result-file=" + outputFileName);
        cmdlist.add(databaseName);
        for (String tableName : tableNames) {
            cmdlist.add(tableName);
        }
        LOG.info("start dumping " + databaseName + " ...");
        //启动线程监控outputFile的大小
        File outputFile = new File(outputFileName);
        timer.schedule(new FileSizeMonitor(outputFile), 0, 5000);
        String output = _executeByProcessBuilder(cmdlist);
        timer.cancel();
        //取消后，再监控一次文件大小
        monitorFileSize(outputFile);
        return output;
    }

    private String _mysqlload(String databaseName) throws ExecuteException, IOException, InterruptedException {
        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add("sh");
        cmdlist.add(SyncServerConfig.getInstance().getTempDir() + "/shell/mysqlload.sh");
        cmdlist.add("--default-character-set=utf8");
        cmdlist.add("--user=" + dstDBInstance.getUsername());
        String host = dstDBInstance.getHost();
        int port = dstDBInstance.getPort();
        //cmdlist.add("--user=" + dumpTask.getDestMysqlHost().getUsername());
        //String hostWithPort = dumpTask.getDestMysqlHost().getHost();
        /*
        String host = hostWithPort;
        int port = 3306;
        if (StringUtils.contains(hostWithPort, ':')) {
            String[] splits = hostWithPort.split(":");
            host = splits[0];
            port = Integer.parseInt(splits[1]);
        }*/
        cmdlist.add("--host=" + host);
        cmdlist.add("--port=" + port);
        cmdlist.add("--password=" + dstDBInstance.getPassword());
        //cmdlist.add("--password=" + dumpTask.getDestMysqlHost().getPassword());
        cmdlist.add(_getSourceFile(databaseName));
        LOG.info("start loading " + databaseName + " ...");
        return _executeByProcessBuilder(cmdlist);
    }

    @SuppressWarnings("unused")
    private String _execute(List<String> cmd) throws IOException, InterruptedException {
        LOG.info("execute shell script, cmd is: " + StringUtils.join(cmd, ' '));
        InputStream input = null;
        try {
            proc = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
            input = proc.getInputStream();
            proc.waitFor();
            return IOUtils.toString(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    private String _executeByProcessBuilder(List<String> cmd) throws IOException, InterruptedException {
        LOG.info("execute shell script, cmd is: " + StringUtils.join(cmd, ' '));
        ProcessBuilderWrapper pbd = new ProcessBuilderWrapper(cmd);
        LOG.info("Command has terminated with status: " + pbd.getStatus());
        LOG.info("Output:\n" + pbd.getInfos());
        return pbd.getErrors();
    }

    @SuppressWarnings("unused")
    private String _executeByApache(List<String> cmd) throws ExecuteException, IOException, InterruptedException {
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        CommandLine cmdLine = new CommandLine(cmd.get(0));
        for (int i = 1; i < cmd.size(); i++) {
            cmdLine.addArgument(cmd.get(i));
        }
        LOG.info("execute(by apache) shell script, cmd is: " + cmdLine.toString());
        executor.execute(cmdLine, resultHandler);
        resultHandler.waitFor();
        return outputStream.toString();
    }

    @Override
    public DumpTask getTask() {
        return this.dumpTask;
    }

    private void fail(String detail) {
        state.setStatus(Status.FAILED);
        state.setDetail(detail);
    }

    public void stop() {
        throw new UnsupportedOperationException("DumpTaskExecutor not support stop() method!");
    }

    private void monitorFileSize(File file) {
        String msg;
        if (file.exists()) {
            long bytes = FileUtils.sizeOf(file);
            double mbytes = bytes / 1000000.0;
            msg = "dump file's size is " + mbytes + "M";
        } else {
            msg = "dump file is not exists yet.";
        }
        state.setDetail(msg);
        LOG.info(msg);
    }

    private class FileSizeMonitor extends TimerTask {
        private File file;

        private FileSizeMonitor(File file) {
            super();
            this.file = file;
        }

        @Override
        public void run() {
            monitorFileSize(file);
        }

    }

    public TaskState getTaskState() {
        return state;
    }

    public void setTaskState(TaskState taskState) {
        this.state = taskState;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    //    public static void main(String[] args) throws ExecuteException, IOException, InterruptedException {
    //        //mock dumpConfig
    //        DumpConfig dumpConfig = new DumpConfig();
    //        List<DumpRelation> dumpRelations = new ArrayList<DumpConfig.DumpRelation>();
    //        List<DatabaseMapping> databaseConfigs =  new ArrayList<DatabaseMapping>();
    //        DatabaseMapping r1 = new DatabaseMapping();
    //        //        r1.setSrcDatabaseName("pumatest");
    //        //        r1.setSrcTableNames(Arrays.asList(new String[] { "test1", "test2" }));
    //        //        r1.setDestDatabaseName("pumatest2");
    //        //        r1.setDestTableNames(Arrays.asList(new String[] { "test11", "test22" }));
    //        r1.setFrom("test");
    //        r1.setSrcTableNames(Arrays.asList(new String[] { "test4", "test5" }));
    //        r1.setDestDatabaseName("test");
    //        r1.setDestTableNames(Arrays.asList(new String[] { "test1", "test2" }));
    //        dumpRelations.add(r1);
    //        DumpRelation r2 = new DumpConfig.DumpRelation();
    //        r2.setSrcDatabaseName("pumatest");
    //        r2.setSrcTableNames(Arrays.asList(new String[] { "test1", "test2" }));
    //        r2.setDestDatabaseName("test");
    //        r2.setDestTableNames(Arrays.asList(new String[] { "test3", "test4" }));
    //        dumpRelations.add(r2);
    //        dumpConfig.setDumpRelations(dumpRelations);
    //        DumpSrc src = new DumpSrc();
    //        src.setHost("127.0.0.1");
    //        src.setPassword("root");
    //        src.setPort(3306);
    //        List<String> opts = Arrays.asList(new String[] { "--no-autocommit", " --disable-keys", "--quick",
    //                "--add-drop-database=false", "--add-drop-table=false", "--skip-add-locks", "--default-character-set=utf8",
    //                "--max_allowed_packet=16777216", " --net_buffer_length=16384", "-i", "--master-data=2", "--single-transaction" });
    //        src.setOptions(opts);
    //        src.setUsername("root");
    //        dumpConfig.setSrc(src);
    //        DumpDest dest = new DumpDest();
    //        dest.setHost("192.168.7.43");
    //        dest.setUsername("binlog");
    //        dest.setPassword("binlog");
    //        dest.setPort(3306);
    //        dumpConfig.setDest(dest);
    //        //json
    //        Gson gson = new Gson();
    //        System.out.println(gson.toJson(dumpConfig));
    //
    //        //调用dump
    //        DumpClient dumpClient = new DumpClient(dumpConfig);
    //        System.out.println(dumpClient.dump());
    //
    //    }

}
