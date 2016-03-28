package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.ClientPositionDao;
import com.dianping.puma.biz.entity.ClientPositionEntity;
import com.dianping.puma.biz.service.ClientPositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class ClientPositionServiceImpl implements ClientPositionService {

    private final static Logger logger = LoggerFactory.getLogger(ClientPositionServiceImpl.class);

    @Autowired
    private ClientPositionDao clientPositionDao;

    private Map<String, ClientPositionEntity> positionEntityMap = new ConcurrentHashMap<String, ClientPositionEntity>();

    @Override
    public List<ClientPositionEntity> findAll() {
        return clientPositionDao.findAll();
    }

    @Override
    public ClientPositionEntity find(String clientName) {
        return clientPositionDao.findByClientName(clientName);
    }

    @Override
    public void update(ClientPositionEntity clientPositionEntity, boolean flush) {
        if (flush) {
            positionEntityMap.remove(clientPositionEntity.getClientName());
            insertOrUpdate(clientPositionEntity);
        } else {
            positionEntityMap.put(clientPositionEntity.getClientName(), clientPositionEntity);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void flush() {
        Set<String> keys = positionEntityMap.keySet();
        for (String key : keys) {
            ClientPositionEntity entity = positionEntityMap.remove(key);
            if (entity == null) {
                continue;
            }
            insertOrUpdate(entity);
        }
    }

    private void insertOrUpdate(ClientPositionEntity entity) {
        try {
            entity.setUpdateTime(new Date());
            int updateRow = clientPositionDao.update(entity);
            if (updateRow == 0) {
                clientPositionDao.insert(entity);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void cleanUpTestClients() {
        List<ClientPositionEntity> clients = clientPositionDao.findOldTestClient();
        for (ClientPositionEntity entity : clients) {
            clientPositionDao.delete(entity.getId());
        }
    }
}
