package com.dianping.puma.pumaserver.service.impl;

import com.dianping.puma.biz.entity.ClientPositionEntity;
import com.dianping.puma.biz.service.ClientPositionService;
import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.exception.binlog.BinlogAckException;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbBinlogAckService implements BinlogAckService {

    @Autowired
    private ClientPositionService clientPositionService;


    @Override
    public void save(String clientName, BinlogAck binlogAck) throws BinlogAckException {
        ClientPositionEntity positionEntity = new ClientPositionEntity();

        positionEntity.setClientName(clientName);
        positionEntity.setBinlogFile(binlogAck.getBinlogInfo().getBinlogFile());
        positionEntity.setBinlogPosition(binlogAck.getBinlogInfo().getBinlogPosition());
        positionEntity.setServerId(binlogAck.getBinlogInfo().getServerId());
        positionEntity.setEventIndex(binlogAck.getBinlogInfo().getEventIndex());

        clientPositionService.update(positionEntity);
    }

    @Override
    public BinlogAck load(String clientName) throws BinlogAckException {
        ClientPositionEntity position = clientPositionService.find(clientName);
        if (position == null) {
            return null;
        }

        BinlogAck ack = new BinlogAck();
        ack.setBinlogInfo(new BinlogInfo()
                        .setBinlogFile(position.getBinlogFile())
                        .setBinlogPosition(position.getBinlogPosition())
                        .setServerId(position.getServerId())
                        .setEventIndex(position.getEventIndex())
        );
        return ack;
    }
}
