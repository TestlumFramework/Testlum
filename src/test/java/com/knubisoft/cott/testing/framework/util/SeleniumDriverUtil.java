package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.AbstractCapabilities;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.BrowserStackLogin;
import com.knubisoft.cott.testing.model.global_config.Capabilities;
import com.knubisoft.cott.testing.model.global_config.ConnectionType;
import com.knubisoft.cott.testing.model.global_config.UiConfig;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

@UtilityClass
public class SeleniumDriverUtil {

    private static final String BROWSER_STACK_URL_TEMPLATE = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";

    public String getBrowserStackUrl(final UiConfig uiConfig) {
        BrowserStackLogin browserStack = uiConfig.getBrowserStackLogin();
        if (nonNull(browserStack)) {
            return format(BROWSER_STACK_URL_TEMPLATE, browserStack.getUsername(), browserStack.getAccessKey());
        }
        throw new DefaultFrameworkException("Cannot find BrowserStackLogin configuration");
    }

    public String getMobilebrowserConnectionUrl(final UiConfig uiConfig) {
        return getServerUrl(uiConfig.getMobilebrowser().getConnection(), uiConfig);
    }

    public String getNativeConnectionUrl(final UiConfig uiConfig) {
        return getServerUrl(uiConfig.getNative().getConnection(), uiConfig);
    }

    private String getServerUrl(final ConnectionType connectionType, final UiConfig uiConfig) {
        return nonNull(connectionType.getAppiumServer())
                ? connectionType.getAppiumServer().getServerUrl()
                : getBrowserStackUrl(uiConfig);
    }

    public void setDefaultCapabilities(final AbstractDevice abstractDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        Capabilities capabilities = abstractDevice.getCapabilities();
        if (nonNull(capabilities)) {
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
