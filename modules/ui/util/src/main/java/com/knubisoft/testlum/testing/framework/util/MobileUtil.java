package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.model.global_config.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MobileUtil {

    private final UiConfig uiConfig;
    private final EnvironmentLoader environmentLoader;

    public List<NativeDevice> filterDefaultEnabledNativeDevices() {
        Native nativeSettings = uiConfig.getNative();
        return Objects.nonNull(nativeSettings)
                ? filterEnabledDevices(nativeSettings.getDevices().getDevice())
                : Collections.emptyList();
    }

    public List<MobilebrowserDevice> filterDefaultEnabledMobileBrowserDevices() {
        Mobilebrowser mobilebrowser = uiConfig.getMobilebrowser();
        return Objects.nonNull(mobilebrowser)
                ? filterEnabledDevices(mobilebrowser.getDevices().getDevice())
                : Collections.emptyList();
    }

    private <T extends AbstractDevice> List<T> filterEnabledDevices(final List<T> deviceList) {
        return deviceList.stream().filter(AbstractDevice::isEnabled).toList();
    }

    public boolean isNativeAndMobileBrowserConfigEnabled() {
        return !filterDefaultEnabledMobileBrowserDevices().isEmpty() && !filterDefaultEnabledNativeDevices().isEmpty()
                && ObjectUtils.allNotNull(uiConfig.getMobilebrowser().getConnection().getAppiumServer(),
                uiConfig.getNative().getConnection().getAppiumServer());
    }

    public Optional<MobilebrowserDevice> getMobileBrowserDeviceBy(final String env, final String deviceAlias) {
        return StringUtils.isBlank(deviceAlias)
                ? Optional.empty()
                : environmentLoader.getMobileBrowserSettings(env).get()
                .getDevices().getDevice().stream()
                .filter(MobilebrowserDevice::isEnabled)
                .filter(device -> device.getAlias().equalsIgnoreCase(deviceAlias))
                .findFirst();
    }

    public Optional<NativeDevice> getNativeDeviceBy(final String env, final String deviceAlias) {
        return StringUtils.isBlank(deviceAlias)
                ? Optional.empty()
                : environmentLoader.getNativeSettings(env).get()
                .getDevices().getDevice().stream()
                .filter(device -> device.isEnabled() && device.getAlias().equalsIgnoreCase(deviceAlias))
                .findFirst();
    }

    public String getNativeDeviceInfo(final NativeDevice nativeDevice) {
        if (Objects.nonNull(nativeDevice.getAppiumCapabilities())) {
            return formatAppiumInfo(LogMessage.NATIVE_APPIUM_INFO,
                    nativeDevice, nativeDevice.getAppiumCapabilities());
        }
        return formatBrowserStackInfo(LogMessage.NATIVE_INFO,
                nativeDevice, nativeDevice.getBrowserStackCapabilities());
    }

    public String getMobileBrowserDeviceInfo(final MobilebrowserDevice mobileDevice) {
        if (Objects.nonNull(mobileDevice.getAppiumCapabilities())) {
            return formatAppiumInfo(LogMessage.MOBILE_BROWSER_APPIUM_INFO,
                    mobileDevice, mobileDevice.getAppiumCapabilities());
        }
        return formatBrowserStackInfo(LogMessage.MOBILE_BROWSER_INFO,
                mobileDevice, mobileDevice.getBrowserStackCapabilities());
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
