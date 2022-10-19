package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import io.appium.java_client.AppiumDriver;

import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class NativeDriverFactory implements AbstractDriverFactory {
    @SneakyThrows
    public WebDriver createDriver(final Object nativeDevice) {
        NativeDevice device = (NativeDevice) nativeDevice;
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(device, desiredCapabilities);
        desiredCapabilities.setCapability("appPackage", device.getAppPackage());
        desiredCapabilities.setCapability("appActivity", device.getAppActivity());
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        String serverUrl = GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl();
        WebDriver driver = new AppiumDriver(new URL(serverUrl), desiredCapabilities);
        return driver;
    }

}
