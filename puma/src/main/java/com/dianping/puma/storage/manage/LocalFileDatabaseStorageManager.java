package com.dianping.puma.storage.manage;

import com.dianping.puma.storage.filesystem.FileSystem;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class LocalFileDatabaseStorageManager implements DatabaseStorageManager {

    private static final String BINLOG_L1_INDEX_ROOT_PATH = FileSystem.getL1IndexDir().getAbsolutePath();

    private static final String BINLOG_L2_INDEX_ROOT_PATH = FileSystem.getL2IndexDir().getAbsolutePath();

    private static final String BINLOG_MASTER_STORAGE_ROOT_PATH = FileSystem.getMasterDataDir().getAbsolutePath();

    @Override
    public void delete(String database) {
        try {
            File binlogL1IndexFile = new File(BINLOG_L1_INDEX_ROOT_PATH, database);
            if (binlogL1IndexFile.exists()) {
                FileUtils.forceDelete(binlogL1IndexFile);
            }

            File binlogL2IndexFile = new File(BINLOG_L2_INDEX_ROOT_PATH, database);
            if (binlogL2IndexFile.exists()) {
                FileUtils.forceDelete(binlogL2IndexFile);
            }

            File binlogMasterStorageFile = new File(BINLOG_MASTER_STORAGE_ROOT_PATH, database);
            if (binlogMasterStorageFile.exists()) {
                FileUtils.forceDelete(binlogMasterStorageFile);
            }

        } catch (IOException e) {
            throw new RuntimeException("failed to delete database files.", e);
        }
    }
}
