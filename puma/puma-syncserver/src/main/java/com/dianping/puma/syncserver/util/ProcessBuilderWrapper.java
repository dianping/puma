package com.dianping.puma.syncserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy from: http://thilosdevblog.wordpress.com/2011/11/21/proper-handling-of-the-processbuilder/
 */
public class ProcessBuilderWrapper {
    private StringWriter infos;
    private StringWriter errors;
    private int status;

    public ProcessBuilderWrapper(File directory, List<String> command) throws IOException, InterruptedException {
        infos = new StringWriter();
        errors = new StringWriter();
        ProcessBuilder pb = new ProcessBuilder(command);
        if (directory != null)
            pb.directory(directory);
        Process process = pb.start();
        StreamBoozer seInfo = new StreamBoozer(process.getInputStream(), new PrintWriter(infos, true));
        StreamBoozer seError = new StreamBoozer(process.getErrorStream(), new PrintWriter(errors, true));
        seInfo.start();
        seError.start();
        status = process.waitFor();
    }

    public ProcessBuilderWrapper(List<String> command) throws IOException, InterruptedException {
        this(null, command);
    }

    public String getErrors() {
        return errors.toString();
    }

    public String getInfos() {
        return infos.toString();
    }

    public int getStatus() {
        return status;
    }

    class StreamBoozer extends Thread {
        private InputStream in;
        private PrintWriter pw;

        StreamBoozer(InputStream in, PrintWriter pw) {
            this.in = in;
            this.pw = pw;
        }

        @Override
        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) throws Exception {
        //mysqldump --host=10.1.77.22 --port=3306 --user=binlog --password=binlog --no-autocommit  --disable-keys --quick --add-drop-database=false --no-create-info --add-drop-table=false --skip-add-locks --default-character-set=utf8 --max_allowed_packet=16777216  --net_buffer_length=16384 -i --master-data=2 --single-transaction --result-file=/data/appdatas/puma/syncserver/dump/1f72fdcd-2679-4b74-bc6e-47c512f0430e/TuanGouMail.dump.sql TuanGouMail SMS_Queue1
        List cmd = new ArrayList();
        cmd.add("mysqldump");
        cmd.add("--host=10.1.77.22");
        cmd.add("--port=3306");
        cmd.add("--user=binlog");
        cmd.add("--password=binlog");
        cmd.add("--no-autocommit");
        cmd.add(" --disable-keys");
        cmd.add("TuanGouMail");
        cmd.add("SMS_Queue1");
        ProcessBuilderWrapper pbd = new ProcessBuilderWrapper( cmd);
        System.out.println("Command has terminated with status: " + pbd.getStatus());
        System.out.println("Output:\n" + pbd.getInfos());
        System.out.println("Error: " + pbd.getErrors());
    }
}
