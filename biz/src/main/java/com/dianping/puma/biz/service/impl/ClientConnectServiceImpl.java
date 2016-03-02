package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientConnectDao;
import com.dianping.puma.biz.entity.ClientConfigEntity;
import com.dianping.puma.biz.entity.ClientConnectEntity;
import com.dianping.puma.biz.model.ClientConnect;
import com.dianping.puma.biz.service.ClientConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ClientConnectServiceImpl implements ClientConnectService {

    @Autowired
    Converter converter;

    @Autowired
    ClientConnectDao clientConnectDao;

    @Override
    public void create(String clientName, ClientConnect clientConnect) {
        ClientConnectEntity entity = converter.convert(clientConnect, ClientConnectEntity.class);
        entity.setClientName(clientName);
        clientConnectDao.insert(entity);
    }

    @Override
    public int modify(String clientName, ClientConnect clientConnect) {
        ClientConnectEntity entity = converter.convert(clientConnect, ClientConnectEntity.class);
        entity.setClientName(clientName);
        return clientConnectDao.update(entity);
    }

    @Override
    public void replace(String clientName, ClientConnect clientConnect) {
        int result = modify(clientName, clientConnect) ;
        if (result == 0) {
            create(clientName, clientConnect);
        }
    }

    @Override
    public int remove(String clientName) {
        return clientConnectDao.delete(clientName);
    }
}
