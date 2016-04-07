package com.dianping.puma.web.web;

import com.dianping.puma.alarm.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dto.PumaAlarmServerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by xiaotian.li on 16/4/6.
 * Email: lixiaotian07@gmail.com
 */
@Controller
@RequestMapping(value = {"/servers"})
public class PumaServerController {

    @Autowired
    Converter converter;

    @Autowired
    PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService;

    @Autowired
    PumaAlarmServerLeaderService pumaAlarmServerLeaderService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<PumaAlarmServerDto> read() {
        return null;
    }
}
