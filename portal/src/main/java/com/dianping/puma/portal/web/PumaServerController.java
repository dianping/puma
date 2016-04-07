package com.dianping.puma.portal.web;

import com.dianping.puma.alarm.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.ha.model.AlarmServerLeader;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.portal.constant.ServerStatus;
import com.dianping.puma.portal.device.PumaDevice;
import com.dianping.puma.portal.device.PumaDeviceManager;
import com.dianping.puma.portal.dto.PumaServerDto;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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

    @Autowired
    PumaDeviceManager pumaDeviceManager;

    private Clock clock = new Clock();

    private long heartbeatExpiredInSecond = 30;

    @RequestMapping(method = RequestMethod.GET, value = "/alarm")
    @ResponseBody
    public List<PumaServerDto> readAlarmServers() {
        List<AlarmServerHeartbeat> heartbeats = pumaAlarmServerHeartbeatService.findAll();
        final Map<String, AlarmServerHeartbeat> heartbeatMap = FluentIterable
                .from(heartbeats)
                .uniqueIndex(new Function<AlarmServerHeartbeat, String>() {
                    @Override
                    public String apply(AlarmServerHeartbeat heartbeat) {
                        return heartbeat.getHost();
                    }
                });

        final AlarmServerLeader leader = pumaAlarmServerLeaderService.findLeader();

        List<PumaDevice> legalAlarmServers = pumaDeviceManager.findAlarmDevices();
        return FluentIterable
                .from(legalAlarmServers)
                .transform(new Function<PumaDevice, PumaServerDto>() {
                    @Override
                    public PumaServerDto apply(PumaDevice legalAlarmServer) {
                        PumaServerDto dto = new PumaServerDto();
                        String host = legalAlarmServer.getHost();
                        dto.setHost(host);
                        dto.setHostname(legalAlarmServer.getHostname());

                        AlarmServerHeartbeat heartbeat = heartbeatMap.get(host);
                        if (heartbeat != null) {
                            long heartbeatTimestamp = heartbeat.getHeartbeatTime().getTime() / 1000;
                            long now = clock.getTimestamp();
                            if (now - heartbeatTimestamp > heartbeatExpiredInSecond) {
                                dto.setServerStatus(ServerStatus.OFFLINE);
                            } else {
                                dto.setLoadAverage(heartbeat.getLoadAverage());

                                if (leader == null || !leader.getHost().equals(host)) {
                                    dto.setServerStatus(ServerStatus.STANDBY);
                                } else {
                                    dto.setServerStatus(ServerStatus.ONLINE);
                                }
                            }
                        }

                        return dto;
                    }
                }).toList();
    }
}
