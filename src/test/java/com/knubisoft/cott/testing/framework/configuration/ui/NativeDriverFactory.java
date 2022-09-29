package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.global_config.Platform;
import io.appium.java_client.AppiumDriver;

import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

@UtilityClass
public class NativeDriverFactory{
    @SneakyThrows
    public WebDriver createDriver(final NativeDevice nativeDevice) {
        String serverUrl = GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl();
        WebDriver driver;
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(nativeDevice, desiredCapabilities);
        desiredCapabilities.setCapability("appPackage",nativeDevice.getAppPackage());
        desiredCapabilities.setCapability("appActivity", nativeDevice.getAppActivity());
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        driver = new AppiumDriver(new URL(serverUrl), desiredCapabilities);
        return driver;

    }

}
