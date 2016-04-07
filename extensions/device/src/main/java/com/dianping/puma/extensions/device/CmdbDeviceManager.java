package com.dianping.puma.extensions.device;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.puma.portal.device.PumaDevice;
import com.dianping.puma.portal.device.PumaDeviceManager;
import com.dianping.puma.portal.exception.PumaDeviceException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by xiaotian.li on 16/4/7.
 * Email: lixiaotian07@gmail.com
 */
public class CmdbDeviceManager implements PumaDeviceManager {

    private HttpClient httpClient = HttpClients.createDefault();

    private static final String BASE_URL = "http://api.cmdb.dp/api/v0.1";

    private static final String DEVICE_URL = "/projects/%s/devices";

    private static final String PUMA_ALARM_DEPLOY_PROJECT = "puma-alarm-deploy";

    @Override
    public List<PumaDevice> findAlarmDevices() throws PumaDeviceException {
        try {
            String url = BASE_URL + String.format(DEVICE_URL, encode(PUMA_ALARM_DEPLOY_PROJECT));
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String json = EntityUtils.toString(httpResponse.getEntity());
            DeviceResult bundle = new Gson().fromJson(json, DeviceResult.class);
            if (bundle == null) {
                return Lists.newArrayList();
            } else {
                List<Device> filteredDevices = filterEnvironment(bundle.getDevices());
                return FluentIterable
                        .from(filteredDevices)
                        .transform(new Function<Device, PumaDevice>() {
                            @Override
                            public PumaDevice apply(Device device) {
                                PumaDevice pumaDevice = new PumaDevice();
                                pumaDevice.setHost(device.getPrivate_ip().get(0));
                                pumaDevice.setHostname(device.getHostname());
                                return pumaDevice;
                            }
                        }).toList();
            }

        } catch (Throwable t) {
            throw new PumaDeviceException("Failed to find puma alarm devices.", t);
        }
    }

    @Override
    public List<PumaDevice> findConsumerDevices() throws PumaDeviceException {
        return null;
    }

    @Override
    public List<PumaDevice> findProducerDevices() throws PumaDeviceException {
        return null;
    }

    @Override
    public List<PumaDevice> findWebDevices() throws PumaDeviceException {
        return null;
    }

    private List<Device> filterEnvironment(List<Device> devices) {
        String env = EnvZooKeeperConfig.getEnv();
        if (env == null) {
            return Lists.newArrayList();
        } else if (env.equals("product")) {
            return filterProduct(devices);
        } else if (env.equals("prelease")) {
            return filterPrelease(devices);
        } else if (env.equals("qa")) {
            return filterBeta(devices);
        } else {
            return Lists.newArrayList();
        }
    }

    private List<Device> filterProduct(List<Device> devices) {
        return FluentIterable
                .from(devices)
                .filter(new Predicate<Device>() {
                    @Override
                    public boolean apply(Device device) {
                        return device.getEnv().equals("生产");
                    }
                }).toList();
    }

    private List<Device> filterPrelease(List<Device> devices) {
        return FluentIterable
                .from(devices)
                .filter(new Predicate<Device>() {
                    @Override
                    public boolean apply(Device device) {
                        return device.getEnv().equals("ppe");
                    }
                }).toList();
    }

    private List<Device> filterBeta(List<Device> devices) {
        return FluentIterable
                .from(devices)
                .filter(new Predicate<Device>() {
                    @Override
                    public boolean apply(Device device) {
                        return device.getEnv().equals("beta");
                    }
                }).toList();
    }

    private class DeviceResult {

        private int total;

        private int numFound;

        private List<Device> devices;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getNumFound() {
            return numFound;
        }

        public void setNumFound(int numFound) {
            this.numFound = numFound;
        }

        public List<Device> getDevices() {
            return devices;
        }

        public void setDevices(List<Device> devices) {
            this.devices = devices;
        }
    }

    private class Device {

        private String hostname;

        private List<String> private_ip;

        private String env;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public List<String> getPrivate_ip() {
            return private_ip;
        }

        public void setPrivate_ip(List<String> private_ip) {
            this.private_ip = private_ip;
        }

        public String getEnv() {
            return env;
        }

        public void setEnv(String env) {
            this.env = env;
        }
    }

    private String encode(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }
}
