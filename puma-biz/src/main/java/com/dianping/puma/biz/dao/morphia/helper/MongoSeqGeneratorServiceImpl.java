package com.dianping.puma.biz.dao.morphia.helper;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Leo Liang
 */
//@Service("seqGeneratorService")
public class MongoSeqGeneratorServiceImpl implements SeqGeneratorService {

    private final static String COLUMN_SEQ = "seq";
    private final static String COLUMN_CATEGORY = "category";

    private MongoClient mongoClient;
    private String dbName;
    private String collName;

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setCollName(String collName) {
        this.collName = collName;
    }

    @Override
    public long nextSeq(String category) {
        if (category == null || "".equals(category)) {
            throw new IllegalArgumentException("category can't be null or empty");
        }
        DBObject update = new BasicDBObject("$inc", new BasicDBObject(COLUMN_SEQ, 1L));
        DBObject query = new BasicDBObject(COLUMN_CATEGORY, category);
        return (Long) mongoClient.getMongo().getDB(dbName).getCollection(collName)
                .findAndModify(query, null, null, false, update, true, true).get(COLUMN_SEQ);
    }

}
