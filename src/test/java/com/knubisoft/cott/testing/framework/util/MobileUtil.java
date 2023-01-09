package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.cott.testing.model.global_config.BrowserStackCapabilities;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.MOBILEBROWSER_APPIUM_INFO;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.MOBILEBROWSER_INFO;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.NATIVE_APPIUM_INFO;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.NATIVE_INFO;

@UtilityClass
public class MobileUtil {

    public List<NativeDevice> filterEnabledNativeDevices() {
        return GlobalTestConfigurationProvider.getNativeDevices().stream()
                .filter(NativeDevice::isEnabled)
                .collect(Collectors.toList());
    }

    public List<MobilebrowserDevice> filterEnabledMobilebrowserDevices() {
        return GlobalTestConfigurationProvider.getMobilebrowserDevices().stream()
                .filter(MobilebrowserDevice::isEnabled)
                .collect(Collectors.toList());
    }

    public boolean isNativeAndMobilebrowserConfigEnabled() {
        return !filterEnabledMobilebrowserDevices().isEmpty() && !filterEnabledNativeDevices().isEmpty()
                && ObjectUtils.allNotNull(
                GlobalTestConfigurationProvider.getMobilebrowserSettings().getConnection().getAppiumServer(),
                GlobalTestConfigurationProvider.getNativeSettings().getConnection().getAppiumServer());
    }

    public String getNativeDeviceInfo(final NativeDevice nativeDevice) {
        if (Objects.nonNull(nativeDevice.getAppiumCapabilities())) {
            return formatAppiumInfo(NATIVE_APPIUM_INFO, nativeDevice, nativeDevice.getAppiumCapabilities());
        }
        return formatBrowserStackInfo(NATIVE_INFO, nativeDevice, nativeDevice.getBrowserStackCapabilities());
    }

    public String getMobilebrowserDeviceInfo(final MobilebrowserDevice mobileDevice) {
        if (Objects.nonNull(mobileDevice.getAppiumCapabilities())) {
            return formatAppiumInfo(MOBILEBROWSER_APPIUM_INFO, mobileDevice, mobileDevice.getAppiumCapabilities());
        }
        return formatBrowserStackInfo(MOBILEBROWSER_INFO, mobileDevice, mobileDevice.getBrowserStackCapabilities());
    }

    private String formatAppiumInfo(final String template,
                                    final AbstractDevice device,
                                    final AppiumCapabilities capabilities) {
        return String.format(template,
                capabilities.getDeviceName(),
                device.getPlatformName().value(),
                capabilities.getPlatformVersion(),
                capabilities.getUdid());
    }

    private String formatBrowserStackInfo(final String template,
                                          final AbstractDevice device,
                                          final BrowserStackCapabilities capabilities) {
        return String.format(template,
                capabilities.getDeviceName(),
                device.getPlatformName().value(),
                capabilities.getPlatformVersion());
    }
}
