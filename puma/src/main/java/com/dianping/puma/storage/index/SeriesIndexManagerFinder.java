package com.dianping.puma.storage.index;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class SeriesIndexManagerFinder {

    public static L1SingleReadIndexManager findL1ReadIndexManager(String database) throws IOException {
        Preconditions.checkNotNull(database);
        File file = FileSystem.visitL1IndexFile(database);
        checkFileExists(file);
        return IndexManagerFactory.newL1SingleReadIndexManager(file);
    }

    public static L2SingleReadIndexManager findL2ReadIndexManager(String database, Sequence sequence)
            throws IOException {
        checkSequence(sequence);
        String date = sequence.date();
        int number = sequence.getNumber();
        File file = FileSystem.visitL2IndexFile(database, date, number);
        checkFileExists(file);
        return IndexManagerFactory.newL2SingleReadIndexManager(file);
    }

    public static L1SingleWriteIndexManager findL1WriteIndexManager(String database) throws IOException {
        File file = FileSystem.visitL1IndexFile(database);
        if (file == null) {
            file = FileSystem.nextL1IndexFile(database);
        }
        return IndexManagerFactory.newL1SingleWriteIndexManager(file);
    }

    public static L2SingleWriteIndexManager findNextL2WriteIndexManager(String database) throws IOException {
        File file = FileSystem.nextL2IndexFile(database);
        String date = FileSystem.parseL2IndexDate(file);
        int number = FileSystem.parseL2IndexNumber(file);
        return IndexManagerFactory.newL2SingleWriteIndexManager(file, date, number);
    }

    private static void checkFileExists(File file) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
    }

    private static void checkSequence(Sequence sequence) throws FileNotFoundException {
        if (sequence == null) {
            throw new FileNotFoundException("Sequence: null");
        }
    }
}
