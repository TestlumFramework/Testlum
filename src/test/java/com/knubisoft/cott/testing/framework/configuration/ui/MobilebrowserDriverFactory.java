package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

@UtilityClass
public class MobilebrowserDriverFactory {
    @SneakyThrows
    public WebDriver createDriver(final MobilebrowserDevice mobilebrowserDevice) {
        Mobilebrowser mobilebrowser = GlobalTestConfigurationProvider.provide().getMobilebrowser();
        String serverUrl = mobilebrowser.getAppiumServerUrl();
        WebDriver driver;
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        MobileDriverUtil.setCommonCapabilities(mobilebrowserDevice, desiredCapabilities);
        driver = new AppiumDriver(new URL(serverUrl), desiredCapabilities);
        driver.get(mobilebrowser.getBaseUrl());
        return driver;

    }
}
