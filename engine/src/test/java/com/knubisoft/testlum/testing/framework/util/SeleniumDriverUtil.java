package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.AbstractCapabilities;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.BrowserStackLogin;
import com.knubisoft.testlum.testing.model.global_config.Capabilities;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BROWSER_STACK_CONFIGURATION_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNKNOWN_CONNECTION_TYPE;
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
        throw new DefaultFrameworkException(BROWSER_STACK_CONFIGURATION_NOT_FOUND);
    }

    public String getMobilebrowserConnectionUrl(final UiConfig uiConfig) {
        return getServerUrl(uiConfig.getMobilebrowser().getConnection(), uiConfig);
    }

    public String getNativeConnectionUrl(final UiConfig uiConfig) {
        return getServerUrl(uiConfig.getNative().getConnection(), uiConfig);
    }

    private String getServerUrl(final ConnectionType connectionType, final UiConfig uiConfig) {
        if (Objects.nonNull(connectionType.getAppiumServer())) {
            return connectionType.getAppiumServer().getServerUrl();
        } else if (Objects.nonNull(connectionType.getBrowserStack())) {
            return getBrowserStackUrl(uiConfig);
        }
        throw new DefaultFrameworkException(UNKNOWN_CONNECTION_TYPE, connectionType.getClass().getSimpleName());
    }

    public void setDefaultCapabilities(final AbstractDevice abstractDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("newCommandTimeout", "5000");
        Capabilities capabilities = abstractDevice.getCapabilities();
        if (nonNull(capabilities)) {
            capabilities.getCapability()
                    .forEach(cap -> desiredCapabilities.setCapability(cap.getName(), cap.getValue()));
        }
    }

    public void setCommonCapabilities(final DesiredCapabilities desiredCapabilities,
                                      final AbstractDevice abstractDevice,
                                      final AbstractCapabilities capabilities) {
        desiredCapabilities.setCapability("platformName", abstractDevice.getPlatformName().value());
        desiredCapabilities.setCapability("platformVersion", capabilities.getPlatformVersion());
        desiredCapabilities.setCapability("deviceName", capabilities.getDeviceName());
    }
}
