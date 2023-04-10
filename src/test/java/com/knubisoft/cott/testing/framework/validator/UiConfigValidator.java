package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
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
        validateNativeCapabilities(uiConfig);
        validateMobilebrowserCapabilities(uiConfig);
    }

    private static void validateMobilebrowserCapabilities(final UiConfig uiConfig) {
        Mobilebrowser mobilebrowserSettings = uiConfig.getMobilebrowser();
        ConnectionType mobilebrowserConnection = mobilebrowserSettings.getConnection();
        List<MobilebrowserDevice> mobilebrowserDevices = mobilebrowserSettings.getDevices().getDevice();
        for (MobilebrowserDevice device : mobilebrowserDevices) {
            checkMobilebrowserCapabilities(mobilebrowserConnection, device);
        }
    }

    private static void validateNativeCapabilities(final UiConfig uiConfig) {
        Native nativeSettings = uiConfig.getNative();
        ConnectionType nativeConnection = nativeSettings.getConnection();
        List<NativeDevice> nativeDevices = nativeSettings.getDevices().getDevice();
        for (NativeDevice device : nativeDevices) {
            checkNativeCapabilities(nativeConnection, device);
        }
    }

    private static void checkNativeCapabilities(final ConnectionType connection,
                                                final NativeDevice device) {
        if (Objects.nonNull(connection.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
                throw new DefaultFrameworkException(ExceptionMessage.INVALID_APPIUM_CAPABILITIES);
        }
        if (Objects.nonNull(connection.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
                throw new DefaultFrameworkException(ExceptionMessage.INVALID_BROWSERSTACK_CAPABILITIES);
        }
    }

    private static void checkMobilebrowserCapabilities(final ConnectionType connection,
                                                       final MobilebrowserDevice device) {
        if (Objects.nonNull(connection.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
            throw new DefaultFrameworkException(ExceptionMessage.INVALID_APPIUM_CAPABILITIES);
        }
        if (Objects.nonNull(connection.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
            throw new DefaultFrameworkException(ExceptionMessage.INVALID_BROWSERSTACK_CAPABILITIES);
        }
    }
}
