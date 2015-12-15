package com.dianping.puma.server.registry;

import com.dianping.puma.config.ConfigManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class LionRegistryServiceTest {

    LionRegistryService lionRegistryService;

    @Mock
    ConfigManager configManager;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        lionRegistryService = new LionRegistryService();
        lionRegistryService.configManager = configManager;
    }

    @Test
    public void testParseHostList() {
        String hostListString0 = "0.0.0.0";
        List<String> hostList0 = new ArrayList<String>();
        hostList0.add("0.0.0.0");
        assertThat(hostList0, is(lionRegistryService.parseHostList(hostListString0)));

        String hostListString1 = "0.0.0.0#1.1.1.1";
        List<String> hostList1 = new ArrayList<String>();
        hostList1.add("0.0.0.0");
        hostList1.add("1.1.1.1");
        assertThat(hostList1, is(lionRegistryService.parseHostList(hostListString1)));

        String hostListString2 = "";
        List<String> hostList2 = new ArrayList<String>();
        assertThat(hostList2, is(lionRegistryService.parseHostList(hostListString2)));
    }

    @Test
    public void testBuildHostListString() {
        List<String> hostList0 = new ArrayList<String>();
        hostList0.add("0.0.0.0");
        String hostListString0 = "0.0.0.0";
        assertThat(hostListString0, is(lionRegistryService.buildHostListString(hostList0)));

        List<String> hostList1 = new ArrayList<String>();
        hostList1.add("0.0.0.0");
        hostList1.add("1.1.1.1");
        String hostListString1 = "0.0.0.0#1.1.1.1";
        assertThat(hostListString1, is(lionRegistryService.buildHostListString(hostList1)));

        List<String> hostList2 = new ArrayList<String>();
        String hostListString2 = "";
        assertThat(hostListString2, is(lionRegistryService.buildHostListString(hostList2)));
    }

    @Test
    public void testRegister() {
        doNothing().when(configManager).createConfig(anyString(), anyString(), anyString());
        doNothing().when(configManager).setConfig(anyString(), anyString());
        doReturn(null).when(configManager).getConfig(anyString());

        lionRegistryService.register("0.0.0.0", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(1)).setConfig(anyString(), anyString());
        verify(configManager, times(1)).setConfig(lionRegistryService.buildKey("puma-test-db"), "0.0.0.0");

        doReturn("0.0.0.0").when(configManager).getConfig(anyString());
        lionRegistryService.register("1.1.1.1", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(2)).setConfig(anyString(), anyString());
        verify(configManager, times(1)).setConfig(lionRegistryService.buildKey("puma-test-db"), "0.0.0.0#1.1.1.1");

        doReturn("0.0.0.0#1.1.1.1").when(configManager).getConfig(anyString());
        lionRegistryService.register("2.2.2.2", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(3)).setConfig(anyString(), anyString());
        verify(configManager, times(1)).setConfig(lionRegistryService.buildKey("puma-test-db"), "0.0.0.0#1.1.1.1#2.2.2.2");

        doReturn("0.0.0.0#1.1.1.1#2.2.2.2").when(configManager).getConfig(anyString());
        lionRegistryService.register("1.1.1.1", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(3)).setConfig(anyString(), anyString());
    }

    @Test
    public void testUnregister() {
        doNothing().when(configManager).createConfig(anyString(), anyString(), anyString());
        doNothing().when(configManager).setConfig(anyString(), anyString());
        doReturn(null).when(configManager).getConfig(anyString());

        lionRegistryService.register("0.0.0.0", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(1)).setConfig(anyString(), anyString());
        verify(configManager, times(1)).setConfig(lionRegistryService.buildKey("puma-test-db"), "0.0.0.0");

        doReturn("0.0.0.0").when(configManager).getConfig(anyString());
        lionRegistryService.register("1.1.1.1", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(2)).setConfig(anyString(), anyString());
        verify(configManager, times(1)).setConfig(lionRegistryService.buildKey("puma-test-db"), "0.0.0.0#1.1.1.1");

        doReturn("0.0.0.0#1.1.1.1").when(configManager).getConfig(anyString());
        lionRegistryService.unregister("0.0.0.0", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(3)).setConfig(anyString(), anyString());
        verify(configManager, times(1)).setConfig(lionRegistryService.buildKey("puma-test-db"), "1.1.1.1");

        doReturn("1.1.1.1").when(configManager).getConfig(anyString());
        lionRegistryService.unregister("1.1.1.1", "puma-test-db");
        verify(configManager, times(1)).createConfig(anyString(), anyString(), anyString());
        verify(configManager, times(4)).setConfig(anyString(), anyString());
        verify(configManager, times(1)).setConfig(lionRegistryService.buildKey("puma-test-db"), "");
    }
}