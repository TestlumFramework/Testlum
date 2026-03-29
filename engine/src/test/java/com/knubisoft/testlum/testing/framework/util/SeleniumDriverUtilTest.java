package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SeleniumDriverUtilTest {

    private final SeleniumDriverUtil util = new SeleniumDriverUtil();

    @Nested
    class GetBrowserStackUrl {
        @Test
        void returnsFormattedUrl() {
            UiConfig uiConfig = mock(UiConfig.class);
            BrowserStackLogin login = mock(BrowserStackLogin.class);
            when(uiConfig.getBrowserStackLogin()).thenReturn(login);
            when(login.getUsername()).thenReturn("user1");
            when(login.getAccessKey()).thenReturn("key1");
            String url = util.getBrowserStackUrl(uiConfig);
            assertEquals("https://user1:key1@hub-cloud.browserstack.com/wd/hub", url);
        }

        @Test
        void throwsWhenNoBrowserStackConfig() {
            UiConfig uiConfig = mock(UiConfig.class);
            when(uiConfig.getBrowserStackLogin()).thenReturn(null);
            assertThrows(DefaultFrameworkException.class, () -> util.getBrowserStackUrl(uiConfig));
        }
    }

    @Nested
    class ToUrl {
        @Test
        void convertsValidUrl() {
            URL url = util.toURL("https://example.com");
            assertNotNull(url);
            assertEquals("https://example.com", url.toString());
        }

        @Test
        void throwsForInvalidUrl() {
            assertThrows(DefaultFrameworkException.class, () -> util.toURL("not a url"));
        }
    }

    @Nested
    class SetDefaultCapabilities {
        @Test
        void setsNewCommandTimeout() {
            AbstractDevice device = mock(AbstractDevice.class);
            when(device.getCapabilities()).thenReturn(null);
            DesiredCapabilities caps = new DesiredCapabilities();
            util.setDefaultCapabilities(device, caps);
            assertEquals("5000", caps.getCapability("appium:newCommandTimeout"));
        }

        @Test
        void setsCustomCapabilities() {
            AbstractDevice device = mock(AbstractDevice.class);
            Capabilities capabilities = mock(Capabilities.class);
            Capability cap = mock(Capability.class);
            when(cap.getName()).thenReturn("custom:key");
            when(cap.getValue()).thenReturn("customValue");
            when(capabilities.getCapability()).thenReturn(List.of(cap));
            when(device.getCapabilities()).thenReturn(capabilities);
            DesiredCapabilities caps = new DesiredCapabilities();
            util.setDefaultCapabilities(device, caps);
            assertEquals("customValue", caps.getCapability("custom:key"));
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    class SetCommonCapabilities {
        @Test
        void setsBrowserStackOptions() {
            DesiredCapabilities caps = new DesiredCapabilities();
            AbstractDevice device = mock(AbstractDevice.class);
            AbstractCapabilities capabilities = mock(AbstractCapabilities.class);
            when(capabilities.getPlatformVersion()).thenReturn("14.0");
            when(capabilities.getDeviceName()).thenReturn("iPhone 15");
            util.setCommonCapabilities(caps, device, capabilities);
            Map<String, Object> bstackOptions = (Map<String, Object>) caps.getCapability("bstack:options");
            assertNotNull(bstackOptions);
            assertEquals("14.0", bstackOptions.get("osVersion"));
            assertEquals("iPhone 15", bstackOptions.get("deviceName"));
            assertTrue((Boolean) bstackOptions.get("local"));
        }
    }
}
