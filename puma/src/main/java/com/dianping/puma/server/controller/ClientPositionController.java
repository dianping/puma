package com.dianping.puma.server.controller;

import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Dozer @ 15/8/27
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Controller
@RequestMapping(value = "/client")
public class ClientPositionController {

    private ClientSessionService clientSessionService;

    private BinlogAckService binlogAckService;

    @RequestMapping(value = "/position", method = RequestMethod.POST)
    @ResponseBody
    public void setClientBinlog(String name, @RequestBody BinlogInfo binlogInfo) {
        BinlogAck ack = new BinlogAck();
        ack.setBinlogInfo(binlogInfo);
        binlogAckService.save(name, ack, true);
        clientSessionService.unsubscribe(name);
    }
}
