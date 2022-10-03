package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import io.appium.java_client.AppiumDriver;

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
        desiredCapabilities.setCapability("appPackage", nativeDevice.getAppPackage());
        desiredCapabilities.setCapability("appActivity", nativeDevice.getAppActivity());
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        String serverUrl = GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl();
        WebDriver driver = new AppiumDriver(new URL(serverUrl), desiredCapabilities);
        return driver;
    }

}
