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
import io.appium.java_client.android.options.UiAutomator2Options;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNKNOWN_MOBILE_PLATFORM_NAME;
import static java.util.Objects.nonNull;

@UtilityClass
public class MobilebrowserDriverFactory {

    public WebDriver createDriver(final MobilebrowserDevice mobileDevice) {
        UiAutomator2Options options = new UiAutomator2Options();
        SeleniumDriverUtil.setDefaultCapabilities(mobileDevice, options);
        setCommonCapabilities(mobileDevice, options);
        setPlatformCapabilities(mobileDevice, options);
        return getMobilebrowserWebDriver(options);
    }

    @SneakyThrows
    private WebDriver getMobilebrowserWebDriver(final UiAutomator2Options options) {

        UiConfig uiConfig = GlobalTestConfigurationProvider.get().getUiConfigs().get(EnvManager.currentEnv());
        String serverUrl = SeleniumDriverUtil.getMobilebrowserConnectionUrl(uiConfig);
        Mobilebrowser settings = uiConfig.getMobilebrowser();
        int secondsToWait = settings.getElementAutowait().getSeconds();
        WebDriver driver = new RemoteWebDriver(new URL(serverUrl), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        driver.get(settings.getBaseUrl());
        return driver;
    }

    private void setCommonCapabilities(final MobilebrowserDevice mobileDevice,
                                       final UiAutomator2Options options) {
        if (nonNull(mobileDevice.getAppiumCapabilities())) {
            AppiumCapabilities capabilities = mobileDevice.getAppiumCapabilities();
            SeleniumDriverUtil.setCommonCapabilities(options, mobileDevice, capabilities);
            options.setUdid(capabilities.getUdid());
        } else if (nonNull(mobileDevice.getBrowserStackCapabilities())) {
            SeleniumDriverUtil.setCommonCapabilities(
                    options, mobileDevice, mobileDevice.getBrowserStackCapabilities());
            options.setCapability("appium:browserstack.local", Boolean.TRUE);
            options.setCapability("appium:browserstack.use_w3c", Boolean.TRUE);
        }
    }

    private void setPlatformCapabilities(final MobilebrowserDevice mobileDevice,
                                         final UiAutomator2Options options) {
        if (Platform.ANDROID == mobileDevice.getPlatformName()) {
            options.setAutomationName("uiautomator2");
            options.withBrowserName("chrome");
            options.setPlatformName(mobileDevice.getPlatformName().value());
        } else if (Platform.IOS == mobileDevice.getPlatformName()) {
            options.setAutomationName("XCUITest");
            options.withBrowserName("safari");
            options.setPlatformName(mobileDevice.getPlatformName().value());
        } else {
            throw new DefaultFrameworkException(UNKNOWN_MOBILE_PLATFORM_NAME, mobileDevice.getPlatformName().value());
        }
    }
}
