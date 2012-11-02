package com.dianping.puma.syncserver.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.sync.DumpConfig;
import com.dianping.puma.core.sync.DumpConfig.Src;

/**
 * dump命令需要注意的地方：<br>
 * (1)LOCK TABLES if the --single-transaction option is not used. <br>
 * dump： mysqldump --host=127.0.0.1 --user=root --password=root --databases pumatest --opt --add-drop-database=false
 * --add-drop-table=false --default-character-set=utf8 --result-file=file_name
 * 
 * @author wukezhu
 */
//dump：mysqldump --host=127.0.0.1 --user=root --password=root --opt --add-drop-database=false --add-drop-table=false --default-character-set=utf8 --result-file=file_name <database_name> <table_name> <table_name>
//load：
public class DumpClient {
    private static final Logger LOG = LoggerFactory.getLogger(DumpClient.class);

    private final String uuid;
    private final String outputDir;
    //-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000056', MASTER_LOG_POS=107;
    private final static Pattern BINLOG_LINE_PATTERN = Pattern.compile("^.+LOG_FILE='(.*)',\\s+.+LOG_POS=([0-9]+);$");

    private DumpConfig dumpConfig;

    public DumpClient(DumpConfig dumpConfig) {
        this.uuid = UUID.randomUUID().toString();
        this.outputDir = "/data/appdatas/puma-syncserver/" + uuid + "/";
        new File(outputDir).mkdir();
        this.dumpConfig = dumpConfig;
    }

    //如果一次mysqldump只能指定一个database下的若干table，那么多个database时，得分多次mysqldump。三种方法:
    //（1）为保证不同database的mysqldump的state(binlog)一致，需要手动lock <指定table> read；多次dump完，再unlock read。
    //      但是手动lock read后，dump里自动会无法lock read！！ (好像dump一定会自动 lock read，有没有办法skip？)
    //（2）允许不同database的mysqldump的state(binlog)不一致，这样需要为不同database做dump和PumaClient的追赶  X
    //（3）修改sync.xml时，一次只允许新增/修改一个<database>/<table>，这样mysqldump只处理一个database，所以不存在不一致
    /**
     * 根据dumpConfig，进行dump，并返回binlog位置
     * 
     * @throws IOException
     * @throws ExecuteException
     * @throws InterruptedException
     */
    public List<BinlogPos> dump() throws ExecuteException, IOException, InterruptedException {
        try {
            List<BinlogPos> binlogPosList = new ArrayList<BinlogPos>();

            Executor executor = new DefaultExecutor();
            executor.setExitValue(1);
            ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
            executor.setWatchdog(watchdog);

            Map<String, List<String>> databaseName2TableNames = dumpConfig.getDatabaseName2TableNames();
            for (Map.Entry<String, List<String>> entry : databaseName2TableNames.entrySet()) {
                //执行dump脚本，dump到<dump_tempDir>/<uuid>目录
                DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
                executor.setStreamHandler(streamHandler);
                CommandLine cmdLine = new CommandLine("mysqldump");
                cmdLine.addArgument("--host=" + dumpConfig.getSrc().getHost());
                cmdLine.addArgument("--user=" + dumpConfig.getSrc().getUsername());
                cmdLine.addArgument("--password=" + dumpConfig.getSrc().getPassword());
                cmdLine.addArgument("--port=" + dumpConfig.getSrc().getPort());
                for (String opt : dumpConfig.getSrc().getOptions()) {
                    cmdLine.addArgument(opt);
                }
                String databaseName = entry.getKey();
                String outputFile = getOutputFile(databaseName);
                cmdLine.addArgument("--result-file=" + outputFile);
                cmdLine.addArgument(databaseName);
                List<String> tableNames = entry.getValue();
                for (String tableName : tableNames) {
                    cmdLine.addArgument(tableName);
                }
                LOG.info("start dumping...");
                LOG.info("cmd is: " + cmdLine.toString());
                executor.execute(cmdLine, resultHandler);
                resultHandler.waitFor();
                String output = outputStream.toString();
                if (StringUtils.isNotBlank(output)) {
                    throw new DumpException("mysqldump output is not empty , so consided to be failed: " + output);
                }
                LOG.info("dump done.");
                BinlogPos binlogPos = new BinlogPos();
                LineIterator lineIterators = IOUtils.lineIterator(new FileInputStream(outputFile), "UTF-8");
                while (lineIterators.hasNext()) {
                    String line = lineIterators.next();
                    //-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000056', MASTER_LOG_POS=107;
                    Matcher matcher = BINLOG_LINE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        binlogPos.setBinlogFile(matcher.group(1));
                        binlogPos.setBinlogPosition(Long.parseLong(matcher.group(2)));
                        break;
                    }
                }
                if (StringUtils.isBlank(binlogPos.getBinlogFile()) || binlogPos.getBinlogPosition() == null) {
                    throw new DumpException("binlogFile or binlogPos is Error: binlogFile=" + binlogPos.getBinlogFile()
                            + ",binlogPos=" + binlogPos.getBinlogPosition());
                }
                LOG.info("binlog info:" + binlogPos);
                binlogPosList.add(binlogPos);
            }
            //load
            

            return binlogPosList;
        } catch (Exception e) {
            throw new DumpException("dump error!", e);
        }
    }

    private String getOutputFile(String databaseName) {
        return outputDir + databaseName + ".sql";
    }

    public static void main(String[] args) throws ExecuteException, IOException, InterruptedException {
        DumpConfig dumpConfig = new DumpConfig();
        Map<String, List<String>> databaseName2TableNames = new HashMap<String, List<String>>();
        databaseName2TableNames.put("pumatest", Arrays.asList(new String[] { "test1", "test2" }));
        databaseName2TableNames.put("test", Arrays.asList(new String[] { "test5" }));
        dumpConfig.setDatabaseName2TableNames(databaseName2TableNames);
        Src src = new Src();
        src.setHost("127.0.0.1");
        src.setPassword("root");
        src.setPort(3306);
        List<String> opts = Arrays.asList(new String[] { "--no-autocommit", " --disable-keys", "--extended-insert", "--quick",
                "--add-drop-database=false", "--add-drop-table=false", "--skip-add-locks", "--default-character-set=utf8",
                "--max_allowed_packet=16777216", " --net_buffer_length=16384", "-i", "--master-data=2", "--single-transaction", });
        src.setOptions(opts);
        src.setUsername("root");
        dumpConfig.setSrc(src);

        DumpClient dumpClient = new DumpClient(dumpConfig);
        dumpClient.dump();
        
      //-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000056', MASTER_LOG_POS=107;
//        Pattern pp = Pattern.compile("^.+LOG_FILE='(.*)',\\s+.+LOG_POS=([0-9]+);$");
//        Matcher matcher = pp.matcher("-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000056', MASTER_LOG_POS=107;");
//        if (matcher.matches()) {
//            System.out.println(matcher.group(1));
//        }
    }

}
