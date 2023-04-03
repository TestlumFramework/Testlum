package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.cott.testing.model.global_config.AppiumNativeCapabilities;
import com.knubisoft.cott.testing.model.global_config.BrowserStackNativeCapabilities;
import com.knubisoft.cott.testing.model.global_config.GooglePlayLogin;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.global_config.Platform;
import com.knubisoft.cott.testing.model.global_config.UiConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    @SneakyThrows
    private AppiumDriver getNativeWebDriver(final NativeDevice nativeDevice,
                                            final DesiredCapabilities desiredCapabilities) {
        UiConfig uiConfig = GlobalTestConfigurationProvider.getUiConfigs().get(EnvManager.currentEnv());
        String serverUrl = SeleniumDriverUtil.getNativeConnectionUrl(uiConfig);
        if (Platform.ANDROID == nativeDevice.getPlatformName()) {
            setAndroidCapabilities(nativeDevice, desiredCapabilities);
            return new AndroidDriver(new URL(serverUrl), desiredCapabilities);
        } else if (Platform.IOS == nativeDevice.getPlatformName()) {
            setIosCapabilities(nativeDevice, desiredCapabilities);
            return new IOSDriver(new URL(serverUrl), desiredCapabilities);
        }
        throw new DefaultFrameworkException("Unknown mobile platform name: %s", nativeDevice.getPlatformName());
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
        desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "uiautomator2");
    }

    private void setIosCapabilities(final NativeDevice nativeDevice,
                                    final DesiredCapabilities desiredCapabilities) {
        if (nonNull(nativeDevice.getAppiumCapabilities())) {
            setAppiumCapabilities(nativeDevice, desiredCapabilities);
        } else if (nonNull(nativeDevice.getBrowserStackCapabilities())) {
            setBrowserStackCapabilities(nativeDevice, desiredCapabilities);
        }
        desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
    }

    private void setAppiumCapabilities(final NativeDevice nativeDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        AppiumNativeCapabilities capabilities = nativeDevice.getAppiumCapabilities();
        SeleniumDriverUtil.setCommonCapabilities(desiredCapabilities, nativeDevice, capabilities);
        desiredCapabilities.setCapability(MobileCapabilityType.UDID, capabilities.getUdid());
        if (isNotBlank(capabilities.getApp())) {
            desiredCapabilities.setCapability(MobileCapabilityType.APP, capabilities.getApp());
        }
    }

    private void setBrowserStackCapabilities(final NativeDevice nativeDevice,
                                             final DesiredCapabilities desiredCapabilities) {
        BrowserStackNativeCapabilities capabilities = nativeDevice.getBrowserStackCapabilities();
        SeleniumDriverUtil.setCommonCapabilities(desiredCapabilities, nativeDevice, capabilities);
        desiredCapabilities.setCapability(MobileCapabilityType.APP, capabilities.getApp());
        desiredCapabilities.setCapability("browserstack.local", Boolean.TRUE);
    }

    private void setAppiumAndroidApp(final DesiredCapabilities desiredCapabilities,
                                     final AppiumNativeCapabilities capabilities) {
        if (isNoneBlank(capabilities.getAppPackage(), capabilities.getAppActivity())) {
            desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, capabilities.getAppPackage());
            desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, capabilities.getAppActivity());
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
