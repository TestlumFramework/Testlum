package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.UIConfiguration;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.testlum.testing.model.global_config.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class MobileBrowserDriverFactory {

    private final SeleniumDriverUtil seleniumDriverUtil;
    private final UIConfiguration uiConfigs;

    public WebDriver createDriver(final MobilebrowserDevice mobileDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        seleniumDriverUtil.setDefaultCapabilities(mobileDevice, desiredCapabilities);
        setCommonCapabilities(mobileDevice, desiredCapabilities);
        setPlatformCapabilities(mobileDevice, desiredCapabilities);
        return getMobilebrowserWebDriver(desiredCapabilities, uiConfigs);
    }

    @SneakyThrows
    private WebDriver getMobilebrowserWebDriver(final DesiredCapabilities desiredCapabilities,
                                                final UIConfiguration uiConfigs) {
        UiConfig uiConfig = uiConfigs.get(EnvManager.currentEnv());
        String serverUrl = seleniumDriverUtil.getMobileBrowserConnectionUrl(uiConfig);
        Mobilebrowser settings = uiConfig.getMobilebrowser();
        int secondsToWait = settings.getElementAutowait().getSeconds();
        WebDriver driver = new RemoteWebDriver(new URL(serverUrl), desiredCapabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        driver.get(settings.getBaseUrl());
        return driver;
    }

    private void setCommonCapabilities(final MobilebrowserDevice mobileDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        if (Objects.nonNull(mobileDevice.getAppiumCapabilities())) {
            AppiumCapabilities capabilities = mobileDevice.getAppiumCapabilities();
            seleniumDriverUtil.setCommonCapabilities(desiredCapabilities, mobileDevice, capabilities);
            desiredCapabilities.setCapability("appium:udid", capabilities.getUdid());
        } else if (Objects.nonNull(mobileDevice.getBrowserStackCapabilities())) {
            seleniumDriverUtil.setCommonCapabilities(
                    desiredCapabilities, mobileDevice, mobileDevice.getBrowserStackCapabilities());
            desiredCapabilities.setCapability("appium:browserstack.local", Boolean.TRUE);
            desiredCapabilities.setCapability("appium:browserstack.use_w3c", Boolean.TRUE);
        }
    }

    private void setPlatformCapabilities(final MobilebrowserDevice mobileDevice,
                                         final DesiredCapabilities desiredCapabilities) {
        if (Platform.ANDROID == mobileDevice.getPlatformName()) {
            desiredCapabilities.setCapability("appium:automationName", "uiautomator2");
            desiredCapabilities.setCapability("browserName", "chrome");
            desiredCapabilities.setCapability("platformName", mobileDevice.getPlatformName());
        } else if (Platform.IOS == mobileDevice.getPlatformName()) {
            desiredCapabilities.setCapability("appium:automationName", "XCUITest");
            desiredCapabilities.setCapability("browserName", "safari");
            desiredCapabilities.setCapability("platformName", mobileDevice.getPlatformName());
        } else {
            throw new DefaultFrameworkException(
                    ExceptionMessage.UNKNOWN_MOBILE_PLATFORM_NAME, mobileDevice.getPlatformName().value());
        }
    }
}
