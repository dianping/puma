package com.dianping.puma.biz.service.remote;

import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.ClientAckDao;
import com.dianping.puma.biz.entity.ClientAckEntity;
import com.dianping.puma.common.model.ClientAck;
import com.dianping.puma.common.service.PumaClientAckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/3.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class RemoteClientAckService implements PumaClientAckService {

    @Autowired
    Converter converter;

    @Autowired
    ClientAckDao clientAckDao;

    @Override
    public ClientAck find(String clientName) {
        ClientAckEntity entity = clientAckDao.find(clientName);
        return converter.convert(entity, ClientAck.class);
    }

    @Override
    public int update(String clientName, ClientAck clientAck) {
        ClientAckEntity entity = converter.convert(clientAck, ClientAckEntity.class);
        entity.setClientName(clientName);
        return clientAckDao.update(entity);
    }
}
