package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.UIConfiguration;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.testlum.testing.model.global_config.*;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class NativeDriverFactory {

    private final SeleniumDriverUtil seleniumDriverUtil;
    private final UIConfiguration uiConfigs;

    public WebDriver createDriver(final NativeDevice nativeDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        seleniumDriverUtil.setDefaultCapabilities(nativeDevice, desiredCapabilities);
        return getNativeWebDriver(nativeDevice, desiredCapabilities);
    }

    private AppiumDriver getNativeWebDriver(final NativeDevice nativeDevice,
                                            final DesiredCapabilities desiredCapabilities) {
        UiConfig uiConfig = uiConfigs.get(EnvManager.currentEnv());
        String serverUrl = seleniumDriverUtil.getNativeConnectionUrl(uiConfig);
        AppiumDriver driver = newAppiumDriver(nativeDevice, serverUrl, desiredCapabilities);
        int secondsToWait = uiConfig.getNative().getElementAutowait().getSeconds();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        return driver;
    }

    private AppiumDriver newAppiumDriver(final NativeDevice nativeDevice,
                                         final String serverUrl,
                                         final DesiredCapabilities desiredCapabilities) {
        URL url = toURL(serverUrl);
        if (Platform.ANDROID == nativeDevice.getPlatformName()) {
            setAndroidCapabilities(nativeDevice, desiredCapabilities);
            return new AndroidDriver(url, desiredCapabilities);
        } else if (Platform.IOS == nativeDevice.getPlatformName()) {
            setIosCapabilities(nativeDevice, desiredCapabilities);
            return new IOSDriver(url, desiredCapabilities);
        }
        throw new DefaultFrameworkException(
                ExceptionMessage.UNKNOWN_MOBILE_PLATFORM_NAME, nativeDevice.getPlatformName().value());
    }

    private URL toURL(final String serverUrl) {
        try {
            return new URL(serverUrl);
        } catch (MalformedURLException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private void setAndroidCapabilities(final NativeDevice nativeDevice,
                                        final DesiredCapabilities desiredCapabilities) {
        if (Objects.nonNull(nativeDevice.getAppiumCapabilities())) {
            setAppiumCapabilities(nativeDevice, desiredCapabilities);
            setAppiumAndroidApp(desiredCapabilities, nativeDevice.getAppiumCapabilities());
        } else if (Objects.nonNull(nativeDevice.getBrowserStackCapabilities())) {
            setBrowserStackCapabilities(nativeDevice, desiredCapabilities);
            setGooglePlayStoreCredentials(desiredCapabilities,
                    nativeDevice.getBrowserStackCapabilities().getGooglePlayLogin());
        }
        desiredCapabilities.setCapability("appium:automationName", "uiautomator2");
    }

    private void setIosCapabilities(final NativeDevice nativeDevice,
                                    final DesiredCapabilities desiredCapabilities) {
        if (Objects.nonNull(nativeDevice.getAppiumCapabilities())) {
            setAppiumCapabilities(nativeDevice, desiredCapabilities);
        } else if (Objects.nonNull(nativeDevice.getBrowserStackCapabilities())) {
            setBrowserStackCapabilities(nativeDevice, desiredCapabilities);
        }
        desiredCapabilities.setCapability("appium:automationName", "XCUITest");
    }

    private void setAppiumCapabilities(final NativeDevice nativeDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        AppiumNativeCapabilities capabilities = nativeDevice.getAppiumCapabilities();
        seleniumDriverUtil.setCommonCapabilities(desiredCapabilities, nativeDevice, capabilities);
        desiredCapabilities.setCapability("appium:udid", capabilities.getUdid());
        if (StringUtils.isNotBlank(capabilities.getApp())) {
            desiredCapabilities.setCapability("appium:app", capabilities.getApp());
        }
    }

    private void setBrowserStackCapabilities(final NativeDevice nativeDevice,
                                             final DesiredCapabilities desiredCapabilities) {
        BrowserStackNativeCapabilities capabilities = nativeDevice.getBrowserStackCapabilities();
        seleniumDriverUtil.setCommonCapabilities(desiredCapabilities, nativeDevice, capabilities);
        desiredCapabilities.setCapability("appium:app", capabilities.getApp());
        desiredCapabilities.setCapability("appium:browserstack.local", Boolean.TRUE);
    }

    private void setAppiumAndroidApp(final DesiredCapabilities desiredCapabilities,
                                     final AppiumNativeCapabilities capabilities) {
        if (StringUtils.isNoneBlank(capabilities.getAppPackage(), capabilities.getAppActivity())) {
            desiredCapabilities.setCapability("appium:appPackage", capabilities.getAppPackage());
            desiredCapabilities.setCapability("appium:appActivity", capabilities.getAppActivity());
        }
    }

    private void setGooglePlayStoreCredentials(final DesiredCapabilities desiredCapabilities,
                                               final GooglePlayLogin googlePlayLogin) {
        if (Objects.nonNull(googlePlayLogin)) {
            Map<String, String> map = new HashMap<>();
            map.put("username", googlePlayLogin.getEmail());
            map.put("password", googlePlayLogin.getPassword());
            desiredCapabilities.setCapability("browserstack.appStoreConfiguration", map);
        }
    }
}
