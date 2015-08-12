package com.dianping.puma.server.extension.auth;

import com.dianping.puma.MockTest;
import com.dianping.puma.core.config.ConfigChangeListener;
import com.dianping.puma.core.config.ConfigManager;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LionAuthorizationMonitorTest extends MockTest {

	private LionAuthorizationMonitor monitor;

	@Mock
	ConfigManager configManager;

	@Before
	public void before() {
		monitor = new LionAuthorizationMonitor();
		monitor.configManager = configManager;

		doNothing().when(configManager).addConfigChangeListener(anyString(), any(ConfigChangeListener.class));
		doReturn("username").when(configManager).getConfig(LionAuthorizationMonitor.BINLOG_USERNAME_KEY);
		doReturn("password").when(configManager).getConfig(LionAuthorizationMonitor.BINLOG_PASSWORD_KEY);
	}

	@Test
	public void testGet() {
		Authorization authorization0 = monitor.get("0.0.0.0");
		assertTrue(EqualsBuilder.reflectionEquals(new Authorization("username", "password"), authorization0));

		Authorization authorization1 = monitor.get("1.1.1.1");
		assertTrue(EqualsBuilder.reflectionEquals(new Authorization("username", "password"), authorization1));
	}

	@Test
	public void testOnChange() {
		AuthorizationMonitorListener listener0 = mock(AuthorizationMonitorListener.class);
		monitor.addListener("0.0.0.0", listener0);

		assertNotNull(monitor.usernameConfigChangeListener);
		assertNotNull(monitor.passwordConfigChangeListener);

		monitor.usernameConfigChangeListener.onConfigChange("username", "new-username");
		verify(listener0, times(1)).onChange(any(Authorization.class));

		AuthorizationMonitorListener listener1 = mock(AuthorizationMonitorListener.class);
		monitor.addListener("1.1.1.1", listener1);

		monitor.usernameConfigChangeListener.onConfigChange("username", "new-username");
		verify(listener0, times(2)).onChange(any(Authorization.class));
		verify(listener1, times(1)).onChange(any(Authorization.class));

		AuthorizationMonitorListener listener2 = mock(AuthorizationMonitorListener.class);
		monitor.addListener("2.2.2.2", listener2);

		monitor.passwordConfigChangeListener.onConfigChange("password", "new-password");
		verify(listener0, times(3)).onChange(any(Authorization.class));
		verify(listener1, times(2)).onChange(any(Authorization.class));
		verify(listener2, times(1)).onChange(any(Authorization.class));
	}
}