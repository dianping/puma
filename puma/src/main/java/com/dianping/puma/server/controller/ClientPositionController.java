package com.dianping.puma.server.controller;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.eventbus.DefaultEventBus;
import com.dianping.puma.eventbus.event.ClientPositionChangedEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Dozer @ 15/8/27
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Controller
@RequestMapping(value = "/client")
public class ClientPositionController {

    @RequestMapping(value = "/position", method = RequestMethod.POST)
    @ResponseBody
    public void setClientBinlog(String name, @RequestBody BinlogInfo binlogInfo) {
        ClientPositionChangedEvent event = new ClientPositionChangedEvent();
        event.setClientName(name);
        event.setBinlogInfo(binlogInfo);
        DefaultEventBus.INSTANCE.post(event);
    }
}
