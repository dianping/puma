package com.dianping.puma.biz.service.remote;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientConnectDao;
import com.dianping.puma.biz.entity.ClientConnectEntity;
import com.dianping.puma.common.model.ClientConnect;
import com.dianping.puma.common.service.PumaClientConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class RemoteClientConnectService implements PumaClientConnectService {

    @Autowired
    Converter converter;

    @Autowired
    ClientConnectDao clientConnectDao;

    @Override
    public int update(String clientName, ClientConnect clientConnect) {
        ClientConnectEntity entity = converter.convert(clientConnect, ClientConnectEntity.class);
        entity.setClientName(clientName);
        return clientConnectDao.update(entity);
    }
}

