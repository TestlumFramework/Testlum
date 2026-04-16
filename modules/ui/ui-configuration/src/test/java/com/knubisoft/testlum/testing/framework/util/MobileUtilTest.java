package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileUtilTest {

    @Mock
    private UiConfig uiConfig;

    @Mock
    private EnvironmentLoader environmentLoader;

    @InjectMocks
    private MobileUtil mobileUtil;

    @Nested
    class FilterDefaultEnabledNativeDevices {
        @Test
        void returnsEmptyWhenNativeIsNull() {
            when(uiConfig.getNative()).thenReturn(null);
            List<NativeDevice> result = mobileUtil.filterDefaultEnabledNativeDevices();
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsOnlyEnabledDevices() {
            Native nativeSettings = mock(Native.class);
            NativeDevices devices = mock(NativeDevices.class);
            NativeDevice enabled = mock(NativeDevice.class);
            NativeDevice disabled = mock(NativeDevice.class);
            when(enabled.isEnabled()).thenReturn(true);
            when(disabled.isEnabled()).thenReturn(false);
            when(devices.getDevice()).thenReturn(List.of(enabled, disabled));
            when(nativeSettings.getDevices()).thenReturn(devices);
            when(uiConfig.getNative()).thenReturn(nativeSettings);

            List<NativeDevice> result = mobileUtil.filterDefaultEnabledNativeDevices();

            assertEquals(1, result.size());
            assertSame(enabled, result.get(0));
        }

        @Test
        void returnsEmptyWhenAllDevicesDisabled() {
            Native nativeSettings = mock(Native.class);
            NativeDevices devices = mock(NativeDevices.class);
            NativeDevice disabled1 = mock(NativeDevice.class);
            NativeDevice disabled2 = mock(NativeDevice.class);
            when(disabled1.isEnabled()).thenReturn(false);
            when(disabled2.isEnabled()).thenReturn(false);
            when(devices.getDevice()).thenReturn(List.of(disabled1, disabled2));
            when(nativeSettings.getDevices()).thenReturn(devices);
            when(uiConfig.getNative()).thenReturn(nativeSettings);

            List<NativeDevice> result = mobileUtil.filterDefaultEnabledNativeDevices();

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsAllWhenAllEnabled() {
            Native nativeSettings = mock(Native.class);
            NativeDevices devices = mock(NativeDevices.class);
            NativeDevice d1 = mock(NativeDevice.class);
            NativeDevice d2 = mock(NativeDevice.class);
            when(d1.isEnabled()).thenReturn(true);
            when(d2.isEnabled()).thenReturn(true);
            when(devices.getDevice()).thenReturn(List.of(d1, d2));
            when(nativeSettings.getDevices()).thenReturn(devices);
            when(uiConfig.getNative()).thenReturn(nativeSettings);

            List<NativeDevice> result = mobileUtil.filterDefaultEnabledNativeDevices();

            assertEquals(2, result.size());
        }
    }

    @Nested
    class FilterDefaultEnabledMobileBrowserDevices {
        @Test
        void returnsEmptyWhenMobilebrowserIsNull() {
            when(uiConfig.getMobilebrowser()).thenReturn(null);
            List<MobilebrowserDevice> result = mobileUtil.filterDefaultEnabledMobileBrowserDevices();
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsOnlyEnabledMobileBrowserDevices() {
            Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
            MobilebrowserDevices devices = mock(MobilebrowserDevices.class);
            MobilebrowserDevice enabled = mock(MobilebrowserDevice.class);
            MobilebrowserDevice disabled = mock(MobilebrowserDevice.class);
            when(enabled.isEnabled()).thenReturn(true);
            when(disabled.isEnabled()).thenReturn(false);
            when(devices.getDevice()).thenReturn(List.of(enabled, disabled));
            when(mobilebrowser.getDevices()).thenReturn(devices);
            when(uiConfig.getMobilebrowser()).thenReturn(mobilebrowser);

            List<MobilebrowserDevice> result = mobileUtil.filterDefaultEnabledMobileBrowserDevices();

            assertEquals(1, result.size());
            assertSame(enabled, result.get(0));
        }
    }

    @Nested
    class IsNativeAndMobileBrowserConfigEnabled {

        @Test
        void returnsFalseWhenNativeIsNull() {
            Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
            MobilebrowserDevices mbDevices = mock(MobilebrowserDevices.class);
            MobilebrowserDevice mbd = mock(MobilebrowserDevice.class);
            when(mbd.isEnabled()).thenReturn(true);
            when(mbDevices.getDevice()).thenReturn(List.of(mbd));
            when(mobilebrowser.getDevices()).thenReturn(mbDevices);
            when(uiConfig.getMobilebrowser()).thenReturn(mobilebrowser);
            when(uiConfig.getNative()).thenReturn(null);

            assertFalse(mobileUtil.isNativeAndMobileBrowserConfigEnabled());
        }

        @Test
        void returnsFalseWhenMobileBrowserIsNull() {
            when(uiConfig.getMobilebrowser()).thenReturn(null);

            assertFalse(mobileUtil.isNativeAndMobileBrowserConfigEnabled());
        }

        @Test
        void returnsTrueWhenBothHaveEnabledDevicesAndAppiumServer() {
            Native nativeSettings = mock(Native.class);
            NativeDevices nativeDevices = mock(NativeDevices.class);
            NativeDevice nd = mock(NativeDevice.class);
            when(nd.isEnabled()).thenReturn(true);
            when(nativeDevices.getDevice()).thenReturn(List.of(nd));
            when(nativeSettings.getDevices()).thenReturn(nativeDevices);
            ConnectionType nativeConnection = mock(ConnectionType.class);
            when(nativeConnection.getAppiumServer()).thenReturn(mock(AppiumServer.class));
            when(nativeSettings.getConnection()).thenReturn(nativeConnection);

            Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
            MobilebrowserDevices mbDevices = mock(MobilebrowserDevices.class);
            MobilebrowserDevice mbd = mock(MobilebrowserDevice.class);
            when(mbd.isEnabled()).thenReturn(true);
            when(mbDevices.getDevice()).thenReturn(List.of(mbd));
            when(mobilebrowser.getDevices()).thenReturn(mbDevices);
            ConnectionType mbConnection = mock(ConnectionType.class);
            when(mbConnection.getAppiumServer()).thenReturn(mock(AppiumServer.class));
            when(mobilebrowser.getConnection()).thenReturn(mbConnection);

            when(uiConfig.getNative()).thenReturn(nativeSettings);
            when(uiConfig.getMobilebrowser()).thenReturn(mobilebrowser);

            assertTrue(mobileUtil.isNativeAndMobileBrowserConfigEnabled());
        }
    }

    @Nested
    class GetMobileBrowserDeviceBy {
        @Test
        void returnsEmptyForBlankAlias() {
            Optional<MobilebrowserDevice> result = mobileUtil.getMobileBrowserDeviceBy("env", "");
            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyForNullAlias() {
            Optional<MobilebrowserDevice> result = mobileUtil.getMobileBrowserDeviceBy("env", null);
            assertFalse(result.isPresent());
        }

        @Test
        void returnsDeviceWhenFound() {
            Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
            MobilebrowserDevices devices = mock(MobilebrowserDevices.class);
            MobilebrowserDevice device = mock(MobilebrowserDevice.class);
            when(device.isEnabled()).thenReturn(true);
            when(device.getAlias()).thenReturn("pixel");
            when(devices.getDevice()).thenReturn(List.of(device));
            when(mobilebrowser.getDevices()).thenReturn(devices);
            when(environmentLoader.getMobileBrowserSettings("dev")).thenReturn(Optional.of(mobilebrowser));

            Optional<MobilebrowserDevice> result = mobileUtil.getMobileBrowserDeviceBy("dev", "pixel");

            assertTrue(result.isPresent());
            assertSame(device, result.get());
        }

        @Test
        void returnsEmptyWhenDeviceNotFound() {
            Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
            MobilebrowserDevices devices = mock(MobilebrowserDevices.class);
            MobilebrowserDevice device = mock(MobilebrowserDevice.class);
            when(device.isEnabled()).thenReturn(true);
            when(device.getAlias()).thenReturn("pixel");
            when(devices.getDevice()).thenReturn(List.of(device));
            when(mobilebrowser.getDevices()).thenReturn(devices);
            when(environmentLoader.getMobileBrowserSettings("dev")).thenReturn(Optional.of(mobilebrowser));

            Optional<MobilebrowserDevice> result = mobileUtil.getMobileBrowserDeviceBy("dev", "iphone");

            assertFalse(result.isPresent());
        }

        @Test
        void caseInsensitiveAliasMatch() {
            Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
            MobilebrowserDevices devices = mock(MobilebrowserDevices.class);
            MobilebrowserDevice device = mock(MobilebrowserDevice.class);
            when(device.isEnabled()).thenReturn(true);
            when(device.getAlias()).thenReturn("Pixel");
            when(devices.getDevice()).thenReturn(List.of(device));
            when(mobilebrowser.getDevices()).thenReturn(devices);
            when(environmentLoader.getMobileBrowserSettings("dev")).thenReturn(Optional.of(mobilebrowser));

            Optional<MobilebrowserDevice> result = mobileUtil.getMobileBrowserDeviceBy("dev", "pixel");

            assertTrue(result.isPresent());
        }
    }

    @Nested
    class GetNativeDeviceBy {
        @Test
        void returnsEmptyForBlankAlias() {
            Optional<NativeDevice> result = mobileUtil.getNativeDeviceBy("env", "");
            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyForNullAlias() {
            Optional<NativeDevice> result = mobileUtil.getNativeDeviceBy("env", null);
            assertFalse(result.isPresent());
        }

        @Test
        void returnsDeviceWhenFound() {
            Native nativeSettings = mock(Native.class);
            NativeDevices devices = mock(NativeDevices.class);
            NativeDevice device = mock(NativeDevice.class);
            when(device.isEnabled()).thenReturn(true);
            when(device.getAlias()).thenReturn("iphone");
            when(devices.getDevice()).thenReturn(List.of(device));
            when(nativeSettings.getDevices()).thenReturn(devices);
            when(environmentLoader.getNativeSettings("dev")).thenReturn(Optional.of(nativeSettings));

            Optional<NativeDevice> result = mobileUtil.getNativeDeviceBy("dev", "iphone");

            assertTrue(result.isPresent());
            assertSame(device, result.get());
        }

        @Test
        void skipsDisabledDevices() {
            Native nativeSettings = mock(Native.class);
            NativeDevices devices = mock(NativeDevices.class);
            NativeDevice device = mock(NativeDevice.class);
            when(device.isEnabled()).thenReturn(false);
            when(devices.getDevice()).thenReturn(List.of(device));
            when(nativeSettings.getDevices()).thenReturn(devices);
            when(environmentLoader.getNativeSettings("dev")).thenReturn(Optional.of(nativeSettings));

            Optional<NativeDevice> result = mobileUtil.getNativeDeviceBy("dev", "iphone");

            assertFalse(result.isPresent());
        }
    }

    @Nested
    class GetNativeDeviceInfo {

        @Test
        void formatsAppiumInfo() {
            NativeDevice device = mock(NativeDevice.class);
            AppiumNativeCapabilities caps = mock(AppiumNativeCapabilities.class);
            when(device.getAppiumCapabilities()).thenReturn(caps);
            when(caps.getDeviceName()).thenReturn("Pixel 6");
            when(caps.getPlatformVersion()).thenReturn("13.0");
            when(caps.getUdid()).thenReturn("abc123");
            when(device.getPlatformName()).thenReturn(Platform.ANDROID);

            String info = mobileUtil.getNativeDeviceInfo(device);

            assertTrue(info.contains("Pixel 6"));
            assertTrue(info.contains("android"));
            assertTrue(info.contains("13.0"));
            assertTrue(info.contains("abc123"));
        }

        @Test
        void formatsBrowserStackInfo() {
            NativeDevice device = mock(NativeDevice.class);
            BrowserStackNativeCapabilities caps = mock(BrowserStackNativeCapabilities.class);

            when(device.getAppiumCapabilities()).thenReturn(null);
            when(device.getBrowserStackCapabilities()).thenReturn(caps);
            when(caps.getDeviceName()).thenReturn("iPhone 15");
            when(caps.getPlatformVersion()).thenReturn("17.0");
            when(device.getPlatformName()).thenReturn(Platform.IOS);

            String info = mobileUtil.getNativeDeviceInfo(device);

            assertTrue(info.contains("iPhone 15"));
            assertTrue(info.contains("ios"));
            assertTrue(info.contains("17.0"));
        }
    }

    @Nested
    class GetMobileBrowserDeviceInfo {

        @Test
        void formatsAppiumInfo() {
            MobilebrowserDevice device = mock(MobilebrowserDevice.class);
            AppiumCapabilities caps = mock(AppiumCapabilities.class);

            when(device.getAppiumCapabilities()).thenReturn(caps);
            when(caps.getDeviceName()).thenReturn("Galaxy S23");
            when(caps.getPlatformVersion()).thenReturn("14.0");
            when(caps.getUdid()).thenReturn("xyz789");
            when(device.getPlatformName()).thenReturn(Platform.ANDROID);

            String info = mobileUtil.getMobileBrowserDeviceInfo(device);

            assertTrue(info.contains("Galaxy S23"));
            assertTrue(info.contains("android"));
            assertTrue(info.contains("14.0"));
            assertTrue(info.contains("xyz789"));
        }

        @Test
        void formatsBrowserStackInfo() {
            MobilebrowserDevice device = mock(MobilebrowserDevice.class);
            BrowserStackCapabilities caps = mock(BrowserStackCapabilities.class);

            when(device.getAppiumCapabilities()).thenReturn(null);
            when(device.getBrowserStackCapabilities()).thenReturn(caps);
            when(caps.getDeviceName()).thenReturn("iPad Pro");
            when(caps.getPlatformVersion()).thenReturn("16.0");
            when(device.getPlatformName()).thenReturn(Platform.IOS);

            String info = mobileUtil.getMobileBrowserDeviceInfo(device);

            assertTrue(info.contains("iPad Pro"));
            assertTrue(info.contains("ios"));
            assertTrue(info.contains("16.0"));
        }
    }
}
