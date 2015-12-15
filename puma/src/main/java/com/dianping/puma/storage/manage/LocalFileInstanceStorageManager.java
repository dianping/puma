package com.dianping.puma.storage.manage;

import com.dianping.cat.Cat;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocalFileInstanceStorageManager implements InstanceStorageManager {

    private static final Logger LOG = Logger.getLogger(LocalFileInstanceStorageManager.class);

    private final Map<String, BinlogInfo> binlogInfoMap = new ConcurrentHashMap<String, BinlogInfo>();

    private static final String SUFFIX = ".binlog";

    public static final long DEFAULT_BINLOGPOS = 4L;

    private static final int MAX_FILE_SIZE = 200;

    private static final byte[] BUF_MASK = new byte[MAX_FILE_SIZE];

    private File baseDir;

    private File bakDir;

    @PostConstruct
    public void init() {
        baseDir = FileSystem.getBinlogInfoDir();

        bakDir = FileSystem.getBackupDir();

        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new RuntimeException("Fail to make dir for " + baseDir.getAbsolutePath());
        }

        if (!bakDir.exists() && !bakDir.mkdirs()) {
            throw new RuntimeException("Fail to make dir for " + bakDir.getAbsolutePath());
        }

        String[] configs = baseDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(SUFFIX);
            }
        });
        if (configs != null) {
            for (String config : configs) {
                loadFromFile(file2task(config));
            }
        }
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void saveToFile() {
        for (Map.Entry<String, BinlogInfo> entry : binlogInfoMap.entrySet()) {
            FileOutputStream writer = null;
            try {
                File f = new File(baseDir, task2file(entry.getKey()));

                writer = new FileOutputStream(f, false);
                writer.write(BUF_MASK);
                writer.close();

                BinlogInfo binlogInfo = entry.getValue();

                writer = new FileOutputStream(f, false);
                writer.write(String.valueOf(binlogInfo.getServerId()).getBytes());
                writer.write("\n".getBytes());
                writer.write((binlogInfo.getBinlogFile() == null ? "" : binlogInfo.getBinlogFile()).getBytes());
                writer.write("\n".getBytes());
                writer.write(String.valueOf(binlogInfo.getBinlogPosition()).getBytes());
                writer.write("\n".getBytes());
                writer.write(String.valueOf(binlogInfo.getEventIndex()).getBytes());
                writer.write("\n".getBytes());
                writer.write(String.valueOf(binlogInfo.getTimestamp()).getBytes());
                writer.write("\n".getBytes());
                writer.flush();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                Cat.logError(e.getMessage(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    public BinlogInfo getBinlogInfo(String taskName) {
        return binlogInfoMap.get(taskName);
    }

    public void setBinlogInfo(String taskName, BinlogInfo binlogInfo) {
        this.binlogInfoMap.put(taskName, binlogInfo);
    }

    public void rename(String oriTaskName, String taskName) {
        binlogInfoMap.put(taskName, binlogInfoMap.remove(oriTaskName));

        File f = new File(bakDir, task2file(oriTaskName));
        if (f.exists() && !f.renameTo(new File(bakDir, task2file(taskName)))) {
            throw new RuntimeException("failed to rename instance task file name.");
        }
    }

    public void remove(String taskName) {
        binlogInfoMap.remove(taskName);
        removeFile(taskName);
    }

    private void removeFile(String taskName) {
        String path = (new File(baseDir, task2file(taskName))).getAbsolutePath();

        // Remove the file to the backup folder.
        File f = new File(path);
        if (f.exists() && f.renameTo(new File(bakDir, genBakFileName(taskName)))) {
            LOG.info("Remove bin log file success.");
        } else {
            LOG.warn("Remove bin log file failure.");
        }
    }

    private void loadFromFile(String taskName) {
        String path = (new File(baseDir, task2file(taskName))).getAbsolutePath();
        File f = new File(path);

        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String serverIdStr = br.readLine();
            String binlogFile = br.readLine();
            String binlogPositionStr = br.readLine();
            String eventIndexStr = br.readLine();
            long timestamp = 0;
            try {
                String timestampStr = br.readLine();
                if (!Strings.isNullOrEmpty(timestampStr)) {
                    timestamp = Long.valueOf(timestampStr);
                }
            } catch (Exception ignore) {
            }

            long serverId = Long.valueOf(serverIdStr);
            long binlogPosition = binlogPositionStr == null ? DEFAULT_BINLOGPOS : Long.parseLong(binlogPositionStr);
            int eventIndex = Integer.parseInt(eventIndexStr);
            BinlogInfo binlogInfo = new BinlogInfo(serverId, binlogFile, binlogPosition, eventIndex, timestamp);

            binlogInfoMap.put(taskName, binlogInfo);

        } catch (Exception e) {
            LOG.error("Read file " + f.getAbsolutePath() + " failed.", e);
            throw new RuntimeException("Read file " + f.getAbsolutePath() + " failed.", e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    LOG.error("Close file " + f.getAbsolutePath() + " failed.");
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.error("Close file " + f.getAbsolutePath() + " failed.");
                }
            }
        }

    }

    private String task2file(String taskName) {
        return taskName + SUFFIX;
    }

    private String file2task(String filename) {
        return filename.substring(0, filename.indexOf(SUFFIX));
    }

    private String genBakFileName(String taskName) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return taskName + '-' + timestamp.getTime() + SUFFIX;
    }
}
