package com.dianping.puma.pumaserver.service;

import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.pumaserver.exception.binlog.BinlogAckException;

public interface BinlogAckService {

    void save(String clientName, BinlogAck binlogAck, boolean flush) throws BinlogAckException;

    BinlogAck load(String clientName) throws BinlogAckException;

    void checkAck(String clientName, BinlogAck binlogAck);
}
