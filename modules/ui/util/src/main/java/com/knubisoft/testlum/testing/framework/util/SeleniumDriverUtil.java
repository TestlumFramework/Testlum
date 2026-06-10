package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class SeleniumDriverUtil {

    private static final String BROWSER_STACK_URL_TEMPLATE = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";

    public String getBrowserStackUrl(final UiConfig uiConfig) {
        BrowserStackLogin browserStack = uiConfig.getBrowserStackLogin();
        if (Objects.nonNull(browserStack)) {
            return String.format(BROWSER_STACK_URL_TEMPLATE, browserStack.getUsername(), browserStack.getAccessKey());
        }
        throw new DefaultFrameworkException(ExceptionMessage.BROWSER_STACK_CONFIGURATION_NOT_FOUND);
    }

    public String getMobileBrowserConnectionUrl(final UiConfig uiConfig) {
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
        throw new DefaultFrameworkException(
                ExceptionMessage.UNKNOWN_CONNECTION_TYPE, connectionType.getClass().getSimpleName());
    }

    public void setDefaultCapabilities(final AbstractDevice abstractDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("appium:newCommandTimeout", "5000");
        Capabilities capabilities = abstractDevice.getCapabilities();
        if (Objects.nonNull(capabilities)) {
            capabilities.getCapability()
                    .forEach(cap -> desiredCapabilities.setCapability(cap.getName(), cap.getValue()));
        }
    }

    public URL toURL(final String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public void setCommonCapabilities(final DesiredCapabilities desiredCapabilities,
                                      final AbstractDevice abstractDevice,
                                      final AbstractCapabilities capabilities) {
        Map<String, Object> browserStackOptions = new HashMap<>();
        browserStackOptions.put("osVersion", capabilities.getPlatformVersion());
        browserStackOptions.put("deviceName", capabilities.getDeviceName());
        browserStackOptions.put("local", Boolean.TRUE);
        desiredCapabilities.setCapability("bstack:options", browserStackOptions);
    }
}
