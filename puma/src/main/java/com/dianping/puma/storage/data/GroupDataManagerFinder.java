package com.dianping.puma.storage.data;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.dianping.puma.storage.utils.DateUtils;

import java.io.File;
import java.io.IOException;

public final class GroupDataManagerFinder {

    public static SingleReadDataManager findMasterReadDataManager(String database, Sequence sequence)
            throws IOException {
        String date = sequence.date();
        int number = sequence.getNumber();

        File file = FileSystem.visitMasterDataFile(database, date, number);

        if (file == null) {
            return null;
        }

        SingleReadDataManager result = DataManagerFactory.newSingleReadDataManager(file);
        result.start();
        result.open(sequence);
        return result;
    }

    public static SingleReadDataManager findNextMasterReadDataManager(String database, Sequence sequence)
            throws IOException {
        String date = sequence.date();
        int number = sequence.getNumber();

        File file = FileSystem.visitMasterDataFile(database, date, ++number);
        if (file == null) {
            number = 0;
            while ((date = DateUtils.getNextDayWithoutFuture(date)) != null) {
                file = FileSystem.visitMasterDataFile(database, date, number);
                if (file != null) {
                    break;
                }
            }
        }

        if (file == null) {
            return null;
        }

        SingleReadDataManager result = DataManagerFactory.newSingleReadDataManager(file);
        result.start();
        result.open(new Sequence(date, number, 0));
        return result;
    }

    public static SingleWriteDataManager findNextMasterWriteDataManager(String database) throws IOException {
        File file = FileSystem.nextMasterDataFile(database);
        String date = FileSystem.parseMasterDataDate(file);
        int number = FileSystem.parseMasterDataNumber(file);

        if (file == null) {
            return null;
        }

        SingleWriteDataManager result = DataManagerFactory.newSingleWriteDataManager(file, date, number);
        result.start();
        return result;
    }
}
