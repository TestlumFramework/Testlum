package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.AbsentCapabilityException;
import com.knubisoft.cott.testing.framework.exception.AbsentConnectionException;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.ConnectionType;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.Native;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class GlobalTestConfigValidator implements XMLValidator<GlobalTestConfiguration> {

    private final SubscriptionValidator subscriptionValidator = new StripeValidationService();

    @Override
    public void validate(final GlobalTestConfiguration globalTestConfig, final File xmlFile) {
        checkIsActiveSubscription(globalTestConfig);
        checkCapabilitiesToConnection(globalTestConfig);
    }

    private void checkIsActiveSubscription(final GlobalTestConfiguration globalTestConfig) {
        if (Objects.isNull(globalTestConfig.getSubscription())) {
            throw new DefaultFrameworkException("Cannot find customer subscription configuration");
        }
        if ("free".equalsIgnoreCase(globalTestConfig.getSubscription().getType().value())) {
            return;
        }
        try {
            subscriptionValidator.checkSubscription(globalTestConfig);
        } catch (Exception e) {
            LogUtil.logException(e);
            throw e;
        }
    }

    private void checkCapabilitiesToConnection(final GlobalTestConfiguration globalTestConfig) {
        Native nativeDevice = globalTestConfig.getNative();
        checkNativeCapabilitiesToConnection(nativeDevice);

        Mobilebrowser mobilebrowser = globalTestConfig.getMobilebrowser();
        checkMobilebrowserCapabilitiesToConnection(mobilebrowser);
    }

    private static void checkNativeCapabilitiesToConnection(final Native nativeDevice) {
        ConnectionType nativeConnection = nativeDevice.getConnection();
        List<NativeDevice> nativeDevices = nativeDevice.getDevices().getDevice();
        for (NativeDevice device : nativeDevices) {
            if (Objects.nonNull(nativeConnection.getAppiumServer())) {
                if (Objects.isNull(device.getAppiumCapabilities())) {
                    throw new AbsentCapabilityException(
                            "Appium capabilities are absent");
                }
            } else if (Objects.nonNull(nativeConnection.getBrowserStack())) {
                if (Objects.isNull(device.getBrowserStackCapabilities())) {
                    throw new AbsentCapabilityException(
                            "BrowserStack capabilities are absent");
                }
            } else {
                throw new AbsentConnectionException(
                        "Connection server is absent");
            }
        }
    }

    private static void checkMobilebrowserCapabilitiesToConnection(final Mobilebrowser mobilebrowser) {
        ConnectionType mobilebrowserConnection = mobilebrowser.getConnection();
        List<MobilebrowserDevice> mobilebrowserDevices = mobilebrowser.getDevices().getDevice();
        for (MobilebrowserDevice device : mobilebrowserDevices) {
            if (Objects.nonNull(mobilebrowserConnection.getAppiumServer())) {
                if (Objects.isNull(device.getAppiumCapabilities())) {
                    throw new AbsentCapabilityException(
                            "Appium capabilities are absent");
                }
            } else if (Objects.nonNull(mobilebrowserConnection.getBrowserStack())) {
                if (Objects.isNull(device.getBrowserStackCapabilities())) {
                    throw new AbsentCapabilityException(
                            "BrowserStack capabilities are absent");
                }
            } else {
                throw new AbsentConnectionException(
                        "Connection server is absent");
            }
        }
    }
}
