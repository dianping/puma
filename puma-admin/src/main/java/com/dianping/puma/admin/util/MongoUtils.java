package com.dianping.puma.admin.util;

import org.apache.commons.lang.StringUtils;
import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;

public class MongoUtils {
    private MongoUtils() {
    }

    public static BSONTimestamp longToBSONTimestamp(Long id) {
        int time = (int) (id >>> 32);
        int inc = (int) (id & 0xFFFFFFFF);
        BSONTimestamp timestamp = new BSONTimestamp(time, inc);
        return timestamp;
    }

    public static Long BSONTimestampToLong(BSONTimestamp timestamp) {
        int time = timestamp.getTime();
        int inc = timestamp.getInc();
        Long id = ((long) time << 32) | inc;
        return id;
    }

    public static Long getLongByCurTime() {
        int time = (int) (System.currentTimeMillis() / 1000);
        BSONTimestamp bst = new BSONTimestamp(time, 1);
        return BSONTimestampToLong(bst);
    }

    public static BSONTimestamp getBSONTimestampByCurTime() {
        int time = (int) (System.currentTimeMillis() / 1000);
        return new BSONTimestamp(time, 1);
    }

    public static ObjectId mergeId2ObjectId(String mergeId) {
        String[] mergeIdSplits = StringUtils.split(mergeId, '_');
        int inc = Integer.parseInt(mergeIdSplits[0]);
        int machine = Integer.parseInt(mergeIdSplits[1]);
        int time = Integer.parseInt(mergeIdSplits[2]);
        return new ObjectId(time, machine, inc);

    }

    public static void main(String[] args) {
        System.out.println(MongoUtils.longToBSONTimestamp(5759378400351354888L));
    }
}
