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
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

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

    private WebDriver getMobilebrowserWebDriver(final DesiredCapabilities desiredCapabilities) {
        UiConfig uiConfig = GlobalTestConfigurationProvider.getUiConfigs().get(EnvManager.currentEnv());
        String serverUrl = SeleniumDriverUtil.getMobilebrowserConnectionUrl(uiConfig);
        Mobilebrowser settings = uiConfig.getMobilebrowser();
        WebDriver driver = newWebDriver(settings, serverUrl, desiredCapabilities);
        int secondsToWait = settings.getElementAutowait().getSeconds();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        driver.get(settings.getBaseUrl());
        return driver;
    }

    @SneakyThrows
    private WebDriver newWebDriver(final Mobilebrowser settings,
                                   final String serverUrl,
                                   final DesiredCapabilities desiredCapabilities) {
        URL url = new URL(serverUrl);
        if (nonNull(settings.getConnection().getAppiumServer())) {
            return new AppiumDriver(url, desiredCapabilities);
        } else if (nonNull(settings.getConnection().getBrowserStack())) {
            return new RemoteWebDriver(url, desiredCapabilities);
        }
        throw new DefaultFrameworkException("Unknown connection type in %s", settings.getClass().getSimpleName());
    }

    private void setCommonCapabilities(final MobilebrowserDevice mobileDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        if (nonNull(mobileDevice.getAppiumCapabilities())) {
            AppiumCapabilities capabilities = mobileDevice.getAppiumCapabilities();
            SeleniumDriverUtil.setCommonCapabilities(desiredCapabilities, mobileDevice, capabilities);
            desiredCapabilities.setCapability(MobileCapabilityType.UDID, capabilities.getUdid());
        } else if (nonNull(mobileDevice.getBrowserStackCapabilities())) {
            SeleniumDriverUtil.setCommonCapabilities(
                    desiredCapabilities, mobileDevice, mobileDevice.getBrowserStackCapabilities());
            desiredCapabilities.setCapability("browserstack.local", Boolean.TRUE);
        }
    }

    private void setPlatformCapabilities(final MobilebrowserDevice mobileDevice,
                                         final DesiredCapabilities desiredCapabilities) {
        if (Platform.ANDROID == mobileDevice.getPlatformName()) {
            desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "uiautomator2");
            desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
        } else if (Platform.IOS == mobileDevice.getPlatformName()) {
            desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
            desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Safari");
        } else {
            throw new DefaultFrameworkException("Unknown mobile platform name: ", mobileDevice.getPlatformName());
        }
    }
}
