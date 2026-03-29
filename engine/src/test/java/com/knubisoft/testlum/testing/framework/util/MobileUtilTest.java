package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
    }

    @Nested
    class FilterDefaultEnabledMobileBrowserDevices {
        @Test
        void returnsEmptyWhenMobilebrowserIsNull() {
            when(uiConfig.getMobilebrowser()).thenReturn(null);
            List<MobilebrowserDevice> result = mobileUtil.filterDefaultEnabledMobileBrowserDevices();
            assertTrue(result.isEmpty());
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
    }
}
