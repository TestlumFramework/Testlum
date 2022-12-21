package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.MOBILEBROWSER_INFO;
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

    public String getNativeDeviceInfo(final NativeDevice nativeDevice) {
        return String.format(NATIVE_INFO,
                nativeDevice.getDeviceName(),
                nativeDevice.getClass().getName(),
                nativeDevice.getUdid());
    }

    public String getMobilebrowserDeviceInfo(final MobilebrowserDevice mobilebrowserDevice) {
        return String.format(MOBILEBROWSER_INFO,
                mobilebrowserDevice.getDeviceName(),
                mobilebrowserDevice.getPlatformName().value());
    }

    public ConnectionType getConnectionType() {
        if (Objects.nonNull(
                GlobalTestConfigurationProvider.provide().getNative().getConnectionType().getAppiumServer())) {
            return ConnectionType.APPIUM;
        }
        return ConnectionType.BROWSER_STACK;
    }
    public enum ConnectionType {
        APPIUM("appium server"),
        BROWSER_STACK("browserStack");

        private final String typeName;

        ConnectionType(final String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }

}
