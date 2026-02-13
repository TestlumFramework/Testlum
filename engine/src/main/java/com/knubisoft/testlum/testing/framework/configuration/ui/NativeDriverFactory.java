package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.testlum.testing.model.global_config.AppiumNativeCapabilities;
import com.knubisoft.testlum.testing.model.global_config.BrowserStackNativeCapabilities;
import com.knubisoft.testlum.testing.model.global_config.GooglePlayLogin;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.Platform;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNKNOWN_MOBILE_PLATFORM_NAME;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
public class NativeDriverFactory {

    public WebDriver createDriver(final NativeDevice nativeDevice) {
        UiAutomator2Options options = new UiAutomator2Options();
        SeleniumDriverUtil.setDefaultCapabilities(nativeDevice, options);
        return getNativeWebDriver(nativeDevice, options);
    }

    private AppiumDriver getNativeWebDriver(final NativeDevice nativeDevice,
                                            final UiAutomator2Options options) {
        UiConfig uiConfig = GlobalTestConfigurationProvider.get().getUiConfigs().get(EnvManager.currentEnv());
        String serverUrl = SeleniumDriverUtil.getNativeConnectionUrl(uiConfig);
        AppiumDriver driver = newAppiumDriver(nativeDevice, serverUrl, options);
        int secondsToWait = uiConfig.getNative().getElementAutowait().getSeconds();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        return driver;
    }

    @SneakyThrows
    private AppiumDriver newAppiumDriver(final NativeDevice nativeDevice,
                                         final String serverUrl,
                                         final UiAutomator2Options options) {
        URL url = new URL(serverUrl);
        if (Platform.ANDROID == nativeDevice.getPlatformName()) {
            setAndroidCapabilities(nativeDevice, options);
            return new AndroidDriver(url, options);
        } else if (Platform.IOS == nativeDevice.getPlatformName()) {
            setIosCapabilities(nativeDevice, options);
            return new IOSDriver(url, options);
        }
        throw new DefaultFrameworkException(UNKNOWN_MOBILE_PLATFORM_NAME, nativeDevice.getPlatformName().value());
    }

    private void setAndroidCapabilities(final NativeDevice nativeDevice,
                                        final UiAutomator2Options options) {
        if (nonNull(nativeDevice.getAppiumCapabilities())) {
            setAppiumCapabilities(nativeDevice, options);
            setAppiumAndroidApp(options, nativeDevice.getAppiumCapabilities());
        } else if (nonNull(nativeDevice.getBrowserStackCapabilities())) {
            setBrowserStackCapabilities(nativeDevice, options);
            setGooglePlayStoreCredentials(options,
                    nativeDevice.getBrowserStackCapabilities().getGooglePlayLogin());
        }
        options.setAutomationName("uiautomator2");
    }

    private void setIosCapabilities(final NativeDevice nativeDevice,
                                    final UiAutomator2Options options) {
        if (nonNull(nativeDevice.getAppiumCapabilities())) {
            setAppiumCapabilities(nativeDevice, options);
        } else if (nonNull(nativeDevice.getBrowserStackCapabilities())) {
            setBrowserStackCapabilities(nativeDevice, options);
        }
        options.setAutomationName("XCUITest");
    }

    private void setAppiumCapabilities(final NativeDevice nativeDevice,
                                       final UiAutomator2Options options) {
        AppiumNativeCapabilities capabilities = nativeDevice.getAppiumCapabilities();
        SeleniumDriverUtil.setCommonCapabilities(options, nativeDevice, capabilities);
        options.setUdid(capabilities.getUdid());
        if (isNotBlank(capabilities.getApp())) {
            options.setApp(capabilities.getApp());
        }
    }

    private void setBrowserStackCapabilities(final NativeDevice nativeDevice,
                                             final UiAutomator2Options options) {
        BrowserStackNativeCapabilities capabilities = nativeDevice.getBrowserStackCapabilities();
        SeleniumDriverUtil.setCommonCapabilities(options, nativeDevice, capabilities);
        options.setApp(capabilities.getApp());
        options.setCapability("appium:browserstack.local", Boolean.TRUE);
    }

    private void setAppiumAndroidApp(final UiAutomator2Options options,
                                     final AppiumNativeCapabilities capabilities) {
        if (isNoneBlank(capabilities.getAppPackage(), capabilities.getAppActivity())) {
            options.setAppPackage(capabilities.getAppPackage());
            options.setAppActivity(capabilities.getAppActivity());
        }
    }

    private void setGooglePlayStoreCredentials(final UiAutomator2Options options,
                                               final GooglePlayLogin googlePlayLogin) {
        if (nonNull(googlePlayLogin)) {
            Map<String, String> map = new HashMap<>();
            map.put("username", googlePlayLogin.getEmail());
            map.put("password", googlePlayLogin.getPassword());
            options.setCapability("browserstack.appStoreConfiguration", map);
        }
    }
}
