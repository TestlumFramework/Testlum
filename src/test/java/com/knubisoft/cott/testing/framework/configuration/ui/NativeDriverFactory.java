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

@UtilityClass
public class NativeDriverFactory {

    @SneakyThrows
    public WebDriver createDriver(final NativeDevice nativeDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(nativeDevice, desiredCapabilities);
        String serverUrl = setServerUrl();
        if (nativeDevice instanceof IosDevice) {
            setIosCaps((IosDevice) nativeDevice, desiredCapabilities);
            return new IOSDriver(new URL(serverUrl), desiredCapabilities);
        }
        setAndroidCaps((AndroidDevice) nativeDevice, desiredCapabilities);
        return new AndroidDriver(new URL(serverUrl), desiredCapabilities);
    }

    private static String setServerUrl() {
        if (GlobalTestConfigurationProvider.provide().getNative().getAppiumServer() != null) {
            return GlobalTestConfigurationProvider.provide().getNative().getAppiumServer().getServerUrl();
        }
        return BrowserStackUtil.getBrowserStackUrl();
    }

    private static void setAndroidCaps(final AndroidDevice nativeDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        MobileDriverUtil.setAutomation(desiredCapabilities, "Android", "uiautomator2");
        if (GlobalTestConfigurationProvider.provide().getNative().getBrowserStack() != null) {
            desiredCapabilities.setCapability(MobileCapabilityType.APP, nativeDevice.getApp());
            desiredCapabilities.setCapability("browserstack.local", "true");
            if (nativeDevice.isPlayMarketLoginEnabled()) {
                setPlayMarketCredentials(desiredCapabilities);
            }
        }
        if (GlobalTestConfigurationProvider.provide().getNative().getAppiumServer() != null) {
            desiredCapabilities.setCapability("appPackage", nativeDevice.getAppPackage());
            desiredCapabilities.setCapability("appActivity", nativeDevice.getAppActivity());
        }
    }

    private static void setPlayMarketCredentials(final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("browserstack.appStoreConfiguration", new HashMap<String, String>() {{
            put("username", GlobalTestConfigurationProvider.getBrowserStack().getPlayMarketLogin().getUsername());
            put("password", GlobalTestConfigurationProvider.getBrowserStack().getPlayMarketLogin().getPassword());
        }});
    }

    private void setIosCaps(final IosDevice nativeDevice,
                            final DesiredCapabilities desiredCapabilities) {
        MobileDriverUtil.setAutomation(desiredCapabilities, "iOS", "XCUITest");
        desiredCapabilities.setCapability(MobileCapabilityType.APP, nativeDevice.getApp());
        if (GlobalTestConfigurationProvider.provide().getNative().getBrowserStack() != null) {
            desiredCapabilities.setCapability("browserstack.local", "true");
        }
    }
}
