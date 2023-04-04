package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.AbsentCapabilityException;
import com.knubisoft.cott.testing.framework.exception.AbsentConnectionException;
import com.knubisoft.cott.testing.model.global_config.ConnectionType;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.Native;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.global_config.UiConfig;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class UiConfigValidator implements XMLValidator<UiConfig> {
    @Override
    public void validate(final UiConfig uiConfig, final File xmlFile) {
        validateCapabilitiesToConnection(uiConfig);
    }

    private void validateCapabilitiesToConnection(final UiConfig uiConfig) {
        Native nativeSettings = uiConfig.getNative();
        checkNativeCapabilitiesToConnection(nativeSettings);

        Mobilebrowser mobilebrowserSettings = uiConfig.getMobilebrowser();
        checkMobilebrowserCapabilitiesToConnection(mobilebrowserSettings);
    }

    private static void checkNativeCapabilitiesToConnection(final Native nativeDevice) {
        ConnectionType connection = nativeDevice.getConnection();
        List<NativeDevice> nativeDevices = nativeDevice.getDevices().getDevice();
        for (NativeDevice device : nativeDevices) {
            checkNativeSettings(connection, device);
        }
    }

    private static void checkNativeSettings(final ConnectionType nativeConnection, final NativeDevice device) {
        if (Objects.nonNull(nativeConnection.getAppiumServer())) {
            if (Objects.isNull(device.getAppiumCapabilities())) {
                throw new AbsentCapabilityException("Appium capabilities are absent");
            }
        } else if (Objects.nonNull(nativeConnection.getBrowserStack())) {
            if (Objects.isNull(device.getBrowserStackCapabilities())) {
                throw new AbsentCapabilityException("BrowserStack capabilities are absent");
            }
        } else {
            throw new AbsentConnectionException("Connection server is absent");
        }
    }

    private static void checkMobilebrowserCapabilitiesToConnection(final Mobilebrowser mobilebrowser) {
        ConnectionType connection = mobilebrowser.getConnection();
        List<MobilebrowserDevice> mobilebrowserDevices = mobilebrowser.getDevices().getDevice();
        for (MobilebrowserDevice device : mobilebrowserDevices) {
            checkMobilebrowserSettings(connection, device);
        }
    }

    private static void checkMobilebrowserSettings(final ConnectionType mobilebrowserConnection,
                                                   final MobilebrowserDevice device) {
        if (Objects.nonNull(mobilebrowserConnection.getAppiumServer())) {
            if (Objects.isNull(device.getAppiumCapabilities())) {
                throw new AbsentCapabilityException("Appium capabilities are absent");
            }
        } else if (Objects.nonNull(mobilebrowserConnection.getBrowserStack())) {
            if (Objects.isNull(device.getBrowserStackCapabilities())) {
                throw new AbsentCapabilityException("BrowserStack capabilities are absent");
            }
        } else {
            throw new AbsentConnectionException("Connection server is absent");
        }
    }
}
