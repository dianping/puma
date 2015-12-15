package com.dianping.puma.pumaserver.channel;

import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;

import java.util.List;

public interface AsyncBinlogChannel {

    void init(
            BinlogInfo binlogInfo,
            String database,
            List<String> tables,
            boolean dml,
            boolean ddl,
            boolean transaction) throws BinlogChannelException;

    void destroy() throws BinlogChannelException;

    /**
     * @param request
     * @return 当前是否有未处理完的请求
     */
    boolean addRequest(BinlogGetRequest request);
}
