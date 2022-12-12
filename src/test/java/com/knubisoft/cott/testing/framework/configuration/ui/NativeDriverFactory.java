package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
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

@UtilityClass
public class NativeDriverFactory {

    @SneakyThrows
    public WebDriver createDriver(final NativeDevice nativeDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(nativeDevice, desiredCapabilities);
        String serverUrl = GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl();
        if (nativeDevice instanceof IosDevice) {
            MobileDriverUtil.setAutomation(desiredCapabilities, "iOS", "XCUITest");
            desiredCapabilities.setCapability(MobileCapabilityType.APP, ((IosDevice) nativeDevice).getApp());
            return new IOSDriver(new URL(serverUrl), desiredCapabilities);
        }
        AndroidDevice device = (AndroidDevice) nativeDevice;
        MobileDriverUtil.setAutomation(desiredCapabilities, "Android", "uiautomator2");
        desiredCapabilities.setCapability("appPackage", device.getAppPackage());
        desiredCapabilities.setCapability("appActivity", device.getAppActivity());
        return new AndroidDriver(new URL(serverUrl), desiredCapabilities);
    }

}
