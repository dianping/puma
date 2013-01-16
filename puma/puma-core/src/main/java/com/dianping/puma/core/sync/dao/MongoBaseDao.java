/**
 * Project: alpaca-admin
 * 
 * File Created at 2012-4-9 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.core.sync.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.sync.model.BaseEntity;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.dao.BasicDAO;
import com.mongodb.WriteConcern;

/**
 * @author Leo Liang
 */
public class MongoBaseDao<T extends BaseEntity> extends BasicDAO<T, Long> {

    @Autowired
    protected SeqGeneratorService seqGeneratorService;

    public MongoBaseDao(Datastore ds) {
        super(ds);
    }

    @Override
    public Key<T> save(T entity) {
        generateIdIfNeeded(entity);
        return ds.save(entity);
    }

    @Override
    public Key<T> save(T entity, WriteConcern wc) {
        generateIdIfNeeded(entity);
        return ds.save(entity, wc);
    }

    /**
     * @param entity
     */
    private void generateIdIfNeeded(T entity) {
        if (entity.getId() == null) {
            entity.setId(seqGeneratorService.nextSeq(entity.getClass().getName()));
        }
    }

}
