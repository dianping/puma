package com.dianping.puma.syncserver.bo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;

import com.dianping.puma.core.sync.Config;
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.DumpConfig;

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

    private final String uuid;

    private DumpConfig dumpConfig;

    public DumpClient(DumpConfig dumpConfig) {
        this.uuid = UUID.randomUUID().toString();
        this.dumpConfig = dumpConfig;
    }

    /**
     * 根据dumpConfig，进行dump，并返回binlog位置
     * 
     * @throws IOException
     * @throws ExecuteException
     * @throws InterruptedException
     */
    public Long dump() throws ExecuteException, IOException, InterruptedException {
        //执行dump脚本，dump到<dump_tempDir>/<uuid>目录
        CommandLine cmdLine = new CommandLine("mysql");
        cmdLine.addArgument("--host=");
        cmdLine.addArgument("--opt");
        cmdLine.addArgument("--add-drop-database=false");
        cmdLine.addArgument("--add-drop-table=false");
        cmdLine.addArgument("--default-character-set=utf8");
        cmdLine.addArgument("--result-file=" + com.dianping.puma.syncserver.conf.Config.getInstance().getDumpTempDir() + "/" + uuid);
        // --add-drop-database=false   --result-file=file_name
        HashMap map = new HashMap();
        map.put("file", new File("invoice.pdf"));
        cmdLine.setSubstitutionMap(map);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        resultHandler.waitFor();

        return null;
    }

}
