package com.dianping.puma.syncserver.job.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.util.ProcessBuilderWrapper;

/**
 * @author wukezhu
 */
public class DumpTaskExecutor implements TaskExecutor<DumpTask> {
    private static final Logger LOG = LoggerFactory.getLogger(DumpTaskExecutor.class);
    private final static Pattern BINLOG_LINE_PATTERN = Pattern.compile("^.+LOG_FILE='(.*)',\\s+.+LOG_POS=([0-9]+);$");

    private final String uuid;
    private final String dumpOutputDir;
    private Thread thread;

    private Executor executor = new DefaultExecutor();
    {
        executor.setExitValue(1);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
        executor.setWatchdog(watchdog);
    }
    private DumpTask dumpTask;

    private Process proc;
    protected TaskExecutorStatus status;

    public DumpTaskExecutor(DumpTask dumpTask) throws IOException {
        this.uuid = UUID.randomUUID().toString();
        this.dumpOutputDir = Config.getInstance().getTempDir() + "/dump/" + uuid + "/";
        FileUtils.forceMkdir(new File(dumpOutputDir));
        this.dumpTask = dumpTask;
        this.status = new TaskExecutorStatus();
        status.setTaskId(dumpTask.getId());
        status.setType(dumpTask.getType());
    }

    /**
     * 根据dumpConfig，进行dump，并返回binlog位置<br>
     */
    @Override
    public void start() {
        thread = new Thread() {
            public void run() {
                LOG.info("started dump.");
                status.setStatus(TaskExecutorStatus.Status.DUMPING);
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
                    if (StringUtils.isNotBlank(output)) {
                        throw new DumpException("mysqldump output is not empty , so consided to be failed: " + output);
                    }
                    LOG.info("dump done.");
                    LineIterator lineIterators = IOUtils.lineIterator(new FileInputStream(_getDumpFile(srcDatabaseName)), "UTF-8");
                    PrintWriter deelFileWriter = new PrintWriter(new File(_getSourceFile(srcDatabaseName)), "UTF-8");
                    deelFileWriter.println("CREATE DATABASE IF NOT EXISTS " + destDatabaseName + ";USE " + destDatabaseName + ";");//添加select database语句
                    while (lineIterators.hasNext()) {
                        String line = lineIterators.next();
                        //获取binlog位置
                        if (status.getBinlogInfo() == null) {
                            Matcher matcher = BINLOG_LINE_PATTERN.matcher(line);
                            if (matcher.matches()) {
                                BinlogInfo binlogInfo = new BinlogInfo();
                                binlogInfo.setBinlogFile(matcher.group(1));
                                binlogInfo.setBinlogPosition(Long.parseLong(matcher.group(2)));
                                status.setBinlogInfo(binlogInfo);
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
                    if (status.getBinlogInfo() == null || StringUtils.isBlank(status.getBinlogInfo().getBinlogFile())
                            || status.getBinlogInfo().getBinlogPosition() <= 0) {
                        throw new DumpException("binlogFile or binlogPos is Error: binlogFile="
                                + status.getBinlogInfo().getBinlogFile() + ",binlogPos="
                                + status.getBinlogInfo().getBinlogPosition());
                    }
                    LOG.info("binlog info:" + status.getBinlogInfo());
                    //(2) load
                    status.setStatus(TaskExecutorStatus.Status.LOADING);
                    LOG.info("started load.");
                    //执行load脚本
                    srcDatabaseName = databaseMapping.getFrom();
                    output = _mysqlload(srcDatabaseName);
                    if (StringUtils.isNotBlank(output)) {
                        throw new DumpException("mysqlload output is not empty , so consided to be failed: " + output);
                    }
                    succeed();
                    LOG.info("load done.");
                } catch (Exception e) {
                    fail(e.getMessage());
                    LOG.error("dump error: " + e.getMessage(), e);
                }
            }
        };
        thread.start();
        status.setStatus(TaskExecutorStatus.Status.RUNNING);
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

    private String _getDumpFile(String databaseName) {
        return dumpOutputDir + databaseName + ".dump.sql";
    }

    private String _getSourceFile(String databaseName) {
        return dumpOutputDir + databaseName + ".source.sql";
    }

    private String _mysqldump(String databaseName, List<String> tableNames) throws IOException, InterruptedException {
        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add("mysqldump");
        String hostWithPort = dumpTask.getSrcMysqlHost().getHost();
        String host = hostWithPort;
        int port = 3306;
        if (StringUtils.contains(hostWithPort, ':')) {
            String[] splits = hostWithPort.split(":");
            host = splits[0];
            port = Integer.parseInt(splits[1]);
        }
        cmdlist.add("--host=" + host);
        cmdlist.add("--port=" + port);
        cmdlist.add("--user=" + dumpTask.getSrcMysqlHost().getUsername());
        cmdlist.add("--password=" + dumpTask.getSrcMysqlHost().getPassword());
        for (String opt : dumpTask.getOptions()) {
            cmdlist.add(opt);
        }
        String outputFile = _getDumpFile(databaseName);
        cmdlist.add("--result-file=" + outputFile);
        cmdlist.add(databaseName);
        for (String tableName : tableNames) {
            cmdlist.add(tableName);
        }
        LOG.info("start dumping " + databaseName + " ...");
        return _executeByProcessBuilder(cmdlist);
    }

    private String _mysqlload(String databaseName) throws ExecuteException, IOException, InterruptedException {
        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add(Config.getInstance().getTempDir() + "/shell/mysqlload.sh");
        cmdlist.add("--user=" + dumpTask.getDestMysqlHost().getUsername());
        String hostWithPort = dumpTask.getDestMysqlHost().getHost();
        String host = hostWithPort;
        int port = 3306;
        if (StringUtils.contains(hostWithPort, ':')) {
            String[] splits = hostWithPort.split(":");
            host = splits[0];
            port = Integer.parseInt(splits[1]);
        }
        cmdlist.add("--host=" + host);
        cmdlist.add("--port=" + port);
        cmdlist.add("--password=" + dumpTask.getDestMysqlHost().getPassword());
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

    @Override
    public void pause(String detail) {
        throw new UnsupportedOperationException("DumpTaskExecutor not support stop() method!");
    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return status;
    }

    @Override
    public void succeed() {
        status.setStatus(TaskExecutorStatus.Status.SUCCEED);
    }

    @Override
    public void fail(String detail) {
        status.setStatus(TaskExecutorStatus.Status.FAILED);
        status.setDetail(detail);
    }

    @Override
    public void disconnect(String detail) {
        throw new UnsupportedOperationException("DumpTaskExecutor not support stop() method!");
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
