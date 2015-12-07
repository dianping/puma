package com.dianping.puma.storage.maintain.clean;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.dianping.puma.storage.index.IndexManagerFactory;
import com.dianping.puma.storage.index.L1SingleReadIndexManager;
import com.dianping.puma.storage.index.L1SingleWriteIndexManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public final class ScheduledDeleteService implements DeleteService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    DeleteStrategy deleteStrategy;

    @Override
    public void delete() {
        cleanL1Index();

        File[] l2DateDirs = FileSystem.visitL2IndexDateDirs();
        for (File l2DateDir : l2DateDirs) {
            delete(l2DateDir);
        }

        File[] dataDateDirs = FileSystem.visitMasterDataDateDirs();
        for (File dataDateDir : dataDateDirs) {
            delete(dataDateDir);
        }
    }

    protected void cleanL1Index() {
        try {
            IndexManagerFactory.pause();
            for (File indexFile : FileSystem.visitAllL1IndexFile()) {
                if (!indexFile.exists()) {
                    continue;
                }

                L1SingleReadIndexManager l1Index = IndexManagerFactory.newL1SingleReadIndexManager(indexFile);
                l1Index.start();
                List<Pair<BinlogInfo, Sequence>> indexes = new ArrayList<Pair<BinlogInfo, Sequence>>();
                Pair<BinlogInfo, Sequence> index;
                while ((index = l1Index.next()) != null) {
                    if (deleteStrategy.canClean(String.valueOf(index.getValue().getCreationDate()))) {
                        continue;
                    }
                    indexes.add(index);
                }
                indexFile.delete();
                l1Index.stop();

                L1SingleWriteIndexManager l1IndexWriter = IndexManagerFactory.newL1SingleWriteIndexManager(indexFile);
                l1IndexWriter.start();

                for (Pair<BinlogInfo, Sequence> pair : indexes) {
                    l1IndexWriter.append(pair.getKey(), pair.getValue());
                }
                l1IndexWriter.flush();

                l1IndexWriter.stop();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            IndexManagerFactory.resume();
        }
    }

    protected void delete(File directory) {
        if (deleteStrategy.canClean(directory.getName())) {
            try {
                deleteDirectory(directory);
            } catch (IOException ignore) {
                logger.error(ignore.getMessage(), ignore);
            }
        }
    }

    protected void deleteDirectory(File directory) throws IOException {
        try {
            FileUtils.deleteDirectory(directory);
        } catch (FileNotFoundException ignore) {
            logger.error(ignore.getMessage(), ignore);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void scheduledDelete() {
        try {
            logger.info("Starting scheduled deleting...");
            delete();
        } catch (Throwable e) {
            logger.error("Scheduled deleting expired files is error.", e);
        }
    }

    public ScheduledDeleteService setDeleteStrategy(DeleteStrategy deleteStrategy) {
        this.deleteStrategy = deleteStrategy;
        return this;
    }
}
