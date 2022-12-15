package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.BrowserStackUtil;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.AndroidDevice;
import com.knubisoft.cott.testing.model.global_config.IosDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.HashMap;

import static com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider.getBrowserStack;

@UtilityClass
public class NativeDriverFactory {

    @SneakyThrows
    public WebDriver createDriver(final NativeDevice nativeDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(nativeDevice, desiredCapabilities);
        String serverUrl = GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl();
        if (nativeDevice instanceof IosDevice) {
            setIosCaps((IosDevice) nativeDevice, desiredCapabilities);
            return new IOSDriver(new URL(GlobalTestConfigurationProvider.getNativeSettings().isBrowserStackEnabled()
                            ? BrowserStackUtil.getBrowserStackUrl() : serverUrl), desiredCapabilities);
        }
        setAndroidCaps((AndroidDevice) nativeDevice, desiredCapabilities);
        return new AndroidDriver(new URL(GlobalTestConfigurationProvider.getNativeSettings().isBrowserStackEnabled()
                        ? BrowserStackUtil.getBrowserStackUrl() : serverUrl), desiredCapabilities);
    }

    private static void setAndroidCaps(final AndroidDevice nativeDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        MobileDriverUtil.setAutomation(desiredCapabilities, "Android", "uiautomator2");
        desiredCapabilities.setCapability(MobileCapabilityType.APP, nativeDevice.getApp());
        desiredCapabilities.setCapability("appPackage", nativeDevice.getAppPackage());
        desiredCapabilities.setCapability("appActivity", nativeDevice.getAppActivity());
        if (nativeDevice.isPlayMarketEnabled()) {
            desiredCapabilities.setCapability("browserstack.appStoreConfiguration", new HashMap<String, String>() {{
                put("username", getBrowserStack().getPlayMarket().getUsername());
                put("password", getBrowserStack().getPlayMarket().getPassword());
            }});
        }
        if (GlobalTestConfigurationProvider.getNativeSettings().isBrowserStackEnabled()) {
            setBrowserStackCaps(desiredCapabilities);
        }
    }

    private void setIosCaps(final IosDevice nativeDevice, final DesiredCapabilities desiredCapabilities) {
        MobileDriverUtil.setAutomation(desiredCapabilities, "iOS", "XCUITest");
        desiredCapabilities.setCapability(MobileCapabilityType.APP, nativeDevice.getApp());
        if (GlobalTestConfigurationProvider.getNativeSettings().isBrowserStackEnabled()) {
            setBrowserStackCaps(desiredCapabilities);
        }
    }

    private void setBrowserStackCaps(final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("browserstack.local", "true");
        BrowserStackUtil.startLocalServer();
    }

}
