package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
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
import io.appium.java_client.ios.IOSDriver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

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
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        SeleniumDriverUtil.setDefaultCapabilities(nativeDevice, desiredCapabilities);
        return getNativeWebDriver(nativeDevice, desiredCapabilities);
    }

    private AppiumDriver getNativeWebDriver(final NativeDevice nativeDevice,
                                            final DesiredCapabilities desiredCapabilities) {
        UiConfig uiConfig = GlobalTestConfigurationProvider.getUiConfigs().get(EnvManager.currentEnv());
        String serverUrl = SeleniumDriverUtil.getNativeConnectionUrl(uiConfig);
        AppiumDriver driver = newAppiumDriver(nativeDevice, serverUrl, desiredCapabilities);
        int secondsToWait = uiConfig.getNative().getElementAutowait().getSeconds();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        return driver;
    }

    @SneakyThrows
    private AppiumDriver newAppiumDriver(final NativeDevice nativeDevice,
                                         final String serverUrl,
                                         final DesiredCapabilities desiredCapabilities) {
        URL url = new URL(serverUrl);
        if (Platform.ANDROID == nativeDevice.getPlatformName()) {
            setAndroidCapabilities(nativeDevice, desiredCapabilities);
            return new AndroidDriver(url, desiredCapabilities);
        } else if (Platform.IOS == nativeDevice.getPlatformName()) {
            setIosCapabilities(nativeDevice, desiredCapabilities);
            return new IOSDriver(url, desiredCapabilities);
        }
        throw new DefaultFrameworkException(UNKNOWN_MOBILE_PLATFORM_NAME, nativeDevice.getPlatformName().value());
    }

    private void setAndroidCapabilities(final NativeDevice nativeDevice,
                                        final DesiredCapabilities desiredCapabilities) {
        if (nonNull(nativeDevice.getAppiumCapabilities())) {
            setAppiumCapabilities(nativeDevice, desiredCapabilities);
            setAppiumAndroidApp(desiredCapabilities, nativeDevice.getAppiumCapabilities());
        } else if (nonNull(nativeDevice.getBrowserStackCapabilities())) {
            setBrowserStackCapabilities(nativeDevice, desiredCapabilities);
            setGooglePlayStoreCredentials(desiredCapabilities,
                    nativeDevice.getBrowserStackCapabilities().getGooglePlayLogin());
        }
        desiredCapabilities.setCapability("appium:automationName", "uiautomator2");
    }

    private void setIosCapabilities(final NativeDevice nativeDevice,
                                    final DesiredCapabilities desiredCapabilities) {
        if (nonNull(nativeDevice.getAppiumCapabilities())) {
            setAppiumCapabilities(nativeDevice, desiredCapabilities);
        } else if (nonNull(nativeDevice.getBrowserStackCapabilities())) {
            setBrowserStackCapabilities(nativeDevice, desiredCapabilities);
        }
        desiredCapabilities.setCapability("appium:automationName", "XCUITest");
    }

    private void setAppiumCapabilities(final NativeDevice nativeDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        AppiumNativeCapabilities capabilities = nativeDevice.getAppiumCapabilities();
        SeleniumDriverUtil.setCommonCapabilities(desiredCapabilities, nativeDevice, capabilities);
        desiredCapabilities.setCapability("appium:udid", capabilities.getUdid());
        if (isNotBlank(capabilities.getApp())) {
            desiredCapabilities.setCapability("appium:app", capabilities.getApp());
        }
    }

    private void setBrowserStackCapabilities(final NativeDevice nativeDevice,
                                             final DesiredCapabilities desiredCapabilities) {
        BrowserStackNativeCapabilities capabilities = nativeDevice.getBrowserStackCapabilities();
        SeleniumDriverUtil.setCommonCapabilities(desiredCapabilities, nativeDevice, capabilities);
        desiredCapabilities.setCapability("appium:app", capabilities.getApp());
        desiredCapabilities.setCapability("appium:browserstack.local", Boolean.TRUE);
    }

    private void setAppiumAndroidApp(final DesiredCapabilities desiredCapabilities,
                                     final AppiumNativeCapabilities capabilities) {
        if (isNoneBlank(capabilities.getAppPackage(), capabilities.getAppActivity())) {
            desiredCapabilities.setCapability("appium:appPackage", capabilities.getAppPackage());
            desiredCapabilities.setCapability("appium:appActivity", capabilities.getAppActivity());
        }
    }

    private void setGooglePlayStoreCredentials(final DesiredCapabilities desiredCapabilities,
                                               final GooglePlayLogin googlePlayLogin) {
        if (nonNull(googlePlayLogin)) {
            Map<String, String> map = new HashMap<>();
            map.put("username", googlePlayLogin.getEmail());
            map.put("password", googlePlayLogin.getPassword());
            desiredCapabilities.setCapability("browserstack.appStoreConfiguration", map);
        }
    }
}
