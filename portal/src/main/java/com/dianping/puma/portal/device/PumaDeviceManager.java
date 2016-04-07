package com.dianping.puma.portal.device;

import com.dianping.puma.portal.exception.PumaDeviceException;

import java.util.List;

/**
 * Created by xiaotian.li on 16/4/7.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaDeviceManager {

    List<PumaDevice> findAlarmDevices() throws PumaDeviceException;

    List<PumaDevice> findConsumerDevices() throws PumaDeviceException;

    List<PumaDevice> findProducerDevices() throws PumaDeviceException;

    List<PumaDevice> findWebDevices() throws PumaDeviceException;
}
