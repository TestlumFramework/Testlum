package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.testlum.testing.model.global_config.BrowserStackCapabilities;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MOBILEBROWSER_APPIUM_INFO;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MOBILEBROWSER_INFO;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.NATIVE_APPIUM_INFO;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.NATIVE_INFO;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@UtilityClass
public class MobileUtil {

    public List<NativeDevice> filterDefaultEnabledNativeDevices() {
        Native aNative = GlobalTestConfigurationProvider.get().getDefaultUiConfigs().getNative();
        return nonNull(aNative)
                ? filterEnabledDevices(aNative.getDevices().getDevice())
                : Collections.emptyList();
    }

    public List<MobilebrowserDevice> filterDefaultEnabledMobilebrowserDevices() {
        Mobilebrowser mobilebrowser = GlobalTestConfigurationProvider.get().getDefaultUiConfigs().getMobilebrowser();
        return nonNull(mobilebrowser)
                ? filterEnabledDevices(mobilebrowser.getDevices().getDevice())
                : Collections.emptyList();
    }

    private <T extends AbstractDevice> List<T> filterEnabledDevices(final List<T> deviceList) {
        return deviceList.stream().filter(AbstractDevice::isEnabled).collect(Collectors.toList());
    }

    public boolean isNativeAndMobilebrowserConfigEnabled() {
        return !filterDefaultEnabledMobilebrowserDevices().isEmpty() && !filterDefaultEnabledNativeDevices().isEmpty()
                && allNotNull(GlobalTestConfigurationProvider.get().getDefaultUiConfigs().getMobilebrowser()
                        .getConnection().getAppiumServer(),
                GlobalTestConfigurationProvider.get().getDefaultUiConfigs().getNative().getConnection().getAppiumServer());
    }

    public Optional<MobilebrowserDevice> getMobilebrowserDeviceBy(final String env, final String deviceAlias) {
        return isBlank(deviceAlias)
                ? Optional.empty()
                : GlobalTestConfigurationProvider.get().getMobilebrowserSettings(env)
                .getDevices().getDevice().stream()
                .filter(MobilebrowserDevice::isEnabled)
                .filter(device -> device.getAlias().equalsIgnoreCase(deviceAlias))
                .findFirst();
    }

    public Optional<NativeDevice> getNativeDeviceBy(final String env, final String deviceAlias) {
        return isBlank(deviceAlias)
                ? Optional.empty()
                : GlobalTestConfigurationProvider.get().getNativeSettings(env)
                .getDevices().getDevice().stream()
                .filter(device -> device.isEnabled() && device.getAlias().equalsIgnoreCase(deviceAlias))
                .findFirst();
    }

    public String getNativeDeviceInfo(final NativeDevice nativeDevice) {
        if (nonNull(nativeDevice.getAppiumCapabilities())) {
            return formatAppiumInfo(NATIVE_APPIUM_INFO, nativeDevice, nativeDevice.getAppiumCapabilities());
        }
        return formatBrowserStackInfo(NATIVE_INFO, nativeDevice, nativeDevice.getBrowserStackCapabilities());
    }

    public String getMobilebrowserDeviceInfo(final MobilebrowserDevice mobileDevice) {
        if (nonNull(mobileDevice.getAppiumCapabilities())) {
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
