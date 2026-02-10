package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.testlum.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.Platform;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNKNOWN_MOBILE_PLATFORM_NAME;
import static java.util.Objects.nonNull;

@UtilityClass
public class MobilebrowserDriverFactory {

    public WebDriver createDriver(final MobilebrowserDevice mobileDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        SeleniumDriverUtil.setDefaultCapabilities(mobileDevice, desiredCapabilities);
        setCommonCapabilities(mobileDevice, desiredCapabilities);
        setPlatformCapabilities(mobileDevice, desiredCapabilities);
        return getMobilebrowserWebDriver(desiredCapabilities);
    }

    @SneakyThrows
    private WebDriver getMobilebrowserWebDriver(final DesiredCapabilities desiredCapabilities) {
        UiConfig uiConfig = GlobalTestConfigurationProvider.get().getUiConfigs().get(EnvManager.currentEnv());
        String serverUrl = SeleniumDriverUtil.getMobilebrowserConnectionUrl(uiConfig);
        Mobilebrowser settings = uiConfig.getMobilebrowser();
        int secondsToWait = settings.getElementAutowait().getSeconds();
        WebDriver driver = new RemoteWebDriver(new URL(serverUrl), desiredCapabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        driver.get(settings.getBaseUrl());
        return driver;
    }

    private void setCommonCapabilities(final MobilebrowserDevice mobileDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        if (nonNull(mobileDevice.getAppiumCapabilities())) {
            AppiumCapabilities capabilities = mobileDevice.getAppiumCapabilities();
            SeleniumDriverUtil.setCommonCapabilities(desiredCapabilities, mobileDevice, capabilities);
            desiredCapabilities.setCapability("appium:udid", capabilities.getUdid());
        } else if (nonNull(mobileDevice.getBrowserStackCapabilities())) {
            SeleniumDriverUtil.setCommonCapabilities(
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
            throw new DefaultFrameworkException(UNKNOWN_MOBILE_PLATFORM_NAME, mobileDevice.getPlatformName().value());
        }
    }
}
