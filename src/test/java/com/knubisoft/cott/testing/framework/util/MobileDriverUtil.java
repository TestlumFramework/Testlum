package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.AbstractCapabilities;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.AppiumServer;
import com.knubisoft.cott.testing.model.global_config.BrowserStackLogin;
import com.knubisoft.cott.testing.model.global_config.Capabilities;
import com.knubisoft.cott.testing.model.global_config.ConnectionType;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Objects;

import static java.lang.String.format;

@UtilityClass
public class MobileDriverUtil {

    private static final String BROWSER_STACK_URL_TEMPLATE = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";

    public String getBrowserStackUrl() {
        BrowserStackLogin browserStack = GlobalTestConfigurationProvider.provide().getBrowserStackLogin();
        if (Objects.nonNull(browserStack)) {
            return format(BROWSER_STACK_URL_TEMPLATE, browserStack.getUsername(), browserStack.getAccessKey());
        }
        throw new DefaultFrameworkException("Cannot find BrowserStackLogin configuration");
    }

    public String getServerUrl(final ConnectionType connectionType) {
        AppiumServer appiumServer = connectionType.getAppiumServer();
        if (Objects.nonNull(appiumServer)) {
            return appiumServer.getServerUrl();
        } else if (Objects.nonNull(connectionType.getBrowserStack())) {
            return getBrowserStackUrl();
        }
        throw new DefaultFrameworkException("Unknown connection type: %s");
    }

    public void setDefaultCapabilities(final AbstractDevice abstractDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        Capabilities capabilities = abstractDevice.getCapabilities();
        if (Objects.nonNull(capabilities)) {
            capabilities.getCapability()
                    .forEach(cap -> desiredCapabilities.setCapability(cap.getName(), cap.getValue()));
        }
    }

    public void setCommonCapabilities(final DesiredCapabilities desiredCapabilities,
                                      final AbstractDevice abstractDevice,
                                      final AbstractCapabilities capabilities) {
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, abstractDevice.getPlatformName().value());
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, capabilities.getPlatformVersion());
        desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, capabilities.getDeviceName());
    }
}
