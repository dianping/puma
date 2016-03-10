package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAckDao;
import com.dianping.puma.biz.entity.ClientAckEntity;
import com.dianping.puma.common.model.ClientAck;
import com.dianping.puma.biz.service.ClientAckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/3.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ClientAckServiceImpl implements ClientAckService {

    @Autowired
    Converter converter;

    @Autowired
    ClientAckDao clientAckDao;

    @Override
    public void create(String clientName, ClientAck clientAck) {
        ClientAckEntity entity = converter.convert(clientAck, ClientAckEntity.class);
        entity.setClientName(clientName);
        clientAckDao.insert(entity);
    }

    @Override
    public int modify(String clientName, ClientAck clientAck) {
        ClientAckEntity entity = converter.convert(clientAck, ClientAckEntity.class);
        entity.setClientName(clientName);
        return clientAckDao.update(entity);
    }

    @Override
    public void replace(String clientName, ClientAck clientAck) {
        int result = modify(clientName, clientAck);
        if (result == 0) {
            create(clientName, clientAck);
        }
    }

    @Override
    public int remove(String clientName) {
        return clientAckDao.delete(clientName);
    }
}
