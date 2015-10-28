package com.dianping.puma.storage.filesystem;

import com.dianping.puma.storage.utils.DateUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public final class FileSystem {

    private static final String L1_INDEX_PREFIX = "l1Index";

    private static final String L1_INDEX_SUFFIX = ".l1idx";

    private static final String L2_INDEX_PREFIX = "bucket-";

    private static final String L2_INDEX_SUFFIX = ".l2idx";

    private static final String MASTER_DATA_PREFIX = "bucket-";

    private static final String MASTER_DATA_SUFFIX = ".data";

    private static final String SLAVE_DATA_PREFIX = "bucket-";

    private static final String SLAVE_DATA_SUFFIX = ".data";

    private static final String DEFAULT_PATH = "/data/appdatas/puma/";

    private static String l1IndexDir;

    private static String l2IndexDir;

    private static String masterDataDir;

    private static String slaveDataDir;

    static {
        changeBasePath(DEFAULT_PATH);
    }

    private FileSystem() {
    }

    public static final void changeBasePath(String base) {
        l1IndexDir = FilenameUtils.concat(base, "binlogIndex/l1Index/");
        l2IndexDir = FilenameUtils.concat(base, "binlogIndex/l2Index/");
        masterDataDir = FilenameUtils.concat(base, "storage/master/");
        slaveDataDir = FilenameUtils.concat(base, "storage/slave/");
    }

    public static final String parseDb(File file) {
        return file.getParentFile().getParentFile().getName();
    }

    public static final String parseDate(File file) {
        return file.getParentFile().getName();
    }

    public static final int parseNumber(File file, String prefix, String suffix) {
        String numberString = StringUtils.substringBetween(file.getName(), prefix, suffix);
        return Integer.valueOf(numberString);
    }

    protected static final int maxFileNumber(String baseDir, String database, String date, String prefix, String suffix) {
        File[] files = visitFiles(baseDir, database, date, prefix, suffix);
        int max = -1;
        for (File file : files) {
            int number = parseNumber(file, prefix, suffix);
            max = number > max ? number : max;
        }
        return max;
    }

    protected static final File visitFile(
            String baseDir, String database, String date, int number, String prefix, String suffix) {
        File databaseDir = new File(baseDir, database);
        File dateDir = new File(databaseDir, date);
        File file = new File(dateDir, genFileName(number, prefix, suffix));

        return file.isFile() ? file : null;
    }

    protected static final File[] visitFiles(
            String baseDir, String database, String date, final String prefix, final String suffix) {
        File databaseDir = new File(baseDir, database);
        File dateDir = new File(databaseDir, date);
        File[] files = dateDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().startsWith(prefix) && file.getName().endsWith(suffix);
            }
        });

        return files == null ? new File[0] : files;
    }

    public static final File getL1IndexDir() {
        return new File(l1IndexDir);
    }

    public static final File getL2IndexDir() {
        return new File(l2IndexDir);
    }

    public static final File getMasterDataDir() {
        return new File(masterDataDir);
    }

    public static final File getSlaveDataDir() {
        return new File(slaveDataDir);
    }

    public static final File nextL1IndexFile(String database) throws IOException {
        File databaseDir = new File(l1IndexDir, database);
        File file = new File(databaseDir, L1_INDEX_PREFIX + L1_INDEX_SUFFIX);
        createFile(file);
        return file;
    }

    public static final File visitL1IndexFile(String database) {
        File databaseDir = new File(l1IndexDir, database);
        File l1Index = new File(databaseDir, genL1IndexName());
        return l1Index.isFile() && l1Index.canRead() && l1Index.canWrite() ? l1Index : null;
    }

    public static final File[] visitL2IndexDateDirs() {
        File[] dateDirs = new File[0];
        File[] databaseDirs = visitDatabaseDirs(l2IndexDir);
        for (File databaseDir : databaseDirs) {
            ArrayUtils.add(dateDirs, visitL2IndexDateDirs(databaseDir.getName()));
        }
        return dateDirs;
    }

    public static final File[] visitL2IndexDateDirs(String database) {
        return visitDateDirs(l2IndexDir, database);
    }

    public static final File visitL2IndexFile(String database, String date, int number) {
        return visitFile(l2IndexDir, database, date, number, L2_INDEX_PREFIX, L2_INDEX_SUFFIX);
    }

    public static final File nextL2IndexFile(String database) throws IOException {
        int max = maxL2IndexFileNumber(database, today());
        return createFile(l2IndexDir, database, today(), max + 1, L2_INDEX_PREFIX, L2_INDEX_SUFFIX);
    }

    public static final File[] visitMasterDataDateDirs() {
        File[] dateDirs = new File[0];
        File[] databaseDirs = visitDatabaseDirs(masterDataDir);
        for (File databaseDir : databaseDirs) {
            ArrayUtils.add(dateDirs, visitMasterDataDateDirs(databaseDir.getName()));
        }
        return dateDirs;
    }

    public static final File[] visitMasterDataDateDirs(String database) {
        return visitDateDirs(masterDataDir, database);
    }

    public static final File visitMasterDataFile(String database, String date, int number) {
        return visitFile(masterDataDir, database, date, number, MASTER_DATA_PREFIX, MASTER_DATA_SUFFIX);
    }

    public static final File visitNextMasterDataFile(String database, String date, int number) {
        File file = visitMasterDataFile(database, date, number + 1);
        if (file != null) {
            return file;
        }

        while ((date = DateUtils.getNextDayWithoutFuture(date)) != null) {
            file = visitMasterDataFile(database, date, 0);
            if (file != null) {
                return file;
            }
        }

        return null;
    }

    public static final File nextMasterDataFile(String database) throws IOException {
        int max = maxMasterFileNumber(database, today());
        return createFile(masterDataDir, database, today(), max + 1, MASTER_DATA_PREFIX, MASTER_DATA_SUFFIX);
    }

    public static final File[] visitSlaveDataDateDirs() {
        File[] dateDirs = new File[0];
        File[] databaseDirs = visitDatabaseDirs(slaveDataDir);
        for (File databaseDir : databaseDirs) {
            ArrayUtils.add(dateDirs, visitSlaveDataDateDirs(databaseDir.getName()));
        }
        return dateDirs;
    }

    public static final File[] visitSlaveDataDateDirs(String database) {
        return visitDateDirs(slaveDataDir, database);
    }

    public static final File visitSlaveDataFile(String database, String date, int number) {
        return visitFile(slaveDataDir, database, date, number, SLAVE_DATA_PREFIX, SLAVE_DATA_SUFFIX);
    }

    public static final File visitNextSlaveDataFile(String database, String date, int number) {
        File file = visitSlaveDataFile(database, date, number);
        if (file != null) {
            return file;
        }

        while ((date = DateUtils.getNextDayWithoutFuture(date)) != null) {
            file = visitSlaveDataFile(database, date, 0);
            if (file != null) {
                return file;
            }
        }

        return null;
    }

    public static final File nextSlaveDataFile(String database) throws IOException {
        int max = maxSlaveFileNumber(database, today());
        return createFile(slaveDataDir, database, today(), max + 1, SLAVE_DATA_PREFIX, SLAVE_DATA_SUFFIX);
    }

    public static final File mapSlaveDatabaseDir(File masterDatabaseDir) {
        String path = masterDatabaseDir.getAbsolutePath();
        String relative = StringUtils.substringAfter(path, masterDataDir);
        return new File(slaveDataDir, relative);
    }

    public static final File mapSlaveDateDir(File masterDateDir) {
        return null;
    }

    public static final File mapSlaveFile(File masterfile) {
        return null;
    }


    protected static final String genL1IndexName() {
        return L1_INDEX_PREFIX + L1_INDEX_SUFFIX;
    }

    protected static final String today() {
        return DateUtils.getNowString();
    }

    protected static final File[] visitDatabaseDirs(String baseDir) {
        File[] files = new File(baseDir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }
        });

        return files == null ? new File[0] : files;
    }

    protected static final File[] visitDateDirs(String baseDir, String database) {
        File databaseDir = new File(baseDir, database);
        File[] files = databaseDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }
        });

        return files == null ? new File[0] : files;
    }

    protected static final String genFileName(int number, String prefix, String suffix) {
        return prefix + number + suffix;
    }

    protected static final int maxL2IndexFileNumber(String database, String date) {
        return maxFileNumber(l2IndexDir, database, date, L2_INDEX_PREFIX, L2_INDEX_SUFFIX);
    }

    protected static final int maxMasterFileNumber(String database, String date) {
        return maxFileNumber(masterDataDir, database, date, MASTER_DATA_PREFIX, MASTER_DATA_SUFFIX);
    }

    protected static final int maxSlaveFileNumber(String database, String date) {
        return maxFileNumber(slaveDataDir, database, date, SLAVE_DATA_PREFIX, SLAVE_DATA_SUFFIX);
    }

    protected static final void createFile(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("failed to create file parent directories.");
        }

        if (!file.createNewFile()) {
            throw new IOException("file already exists.");
        }
    }

    protected static final File createFile(String baseDir, String database, String date, int number, String prefix, String suffix) throws IOException {
        File databaseDir = new File(baseDir, database);
        File dateDir = new File(databaseDir, date);
        File file = new File(dateDir, genFileName(number, prefix, suffix));

        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("failed to create file parent directories.");
        }

        if (!file.createNewFile()) {
            throw new IOException("file already exists.");
        }

        return file;
    }

    public static final String parseMasterDataDb(File file) {
        return parseDb(file);
    }

    public static final String parseMasterDataDate(File file) {
        return parseDate(file);
    }

    public static int parseMasterDataNumber(File file) {
        return parseNumber(file, MASTER_DATA_PREFIX, MASTER_DATA_SUFFIX);
    }

}
