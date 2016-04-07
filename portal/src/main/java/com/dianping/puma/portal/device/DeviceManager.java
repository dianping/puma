package com.dianping.puma.portal.device;

import com.dianping.puma.portal.exception.PumaDeviceException;

import java.util.List;

/**
 * Created by xiaotian.li on 16/4/7.
 * Email: lixiaotian07@gmail.com
 */
public interface DeviceManager {

    List<Device> findAlarmDevices() throws PumaDeviceException;

    List<Device> findConsumerDevices() throws PumaDeviceException;

    List<Device> findProducerDevices() throws PumaDeviceException;

    List<Device> findWebDevices() throws PumaDeviceException;
}
