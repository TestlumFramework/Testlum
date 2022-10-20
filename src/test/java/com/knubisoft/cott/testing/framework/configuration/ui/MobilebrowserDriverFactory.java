package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;


public class MobilebrowserDriverFactory implements AbstractDriverFactory {

    @SneakyThrows
    @Override
    public WebDriver createDriver(final Object mobilebrowserDevice) {
        Mobilebrowser mobilebrowser = GlobalTestConfigurationProvider.provide().getMobilebrowser();
        String serverUrl = mobilebrowser.getAppiumServerUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        MobileDriverUtil.setCommonCapabilities((AbstractDevice) mobilebrowserDevice, desiredCapabilities);
        WebDriver driver = new AppiumDriver(new URL(serverUrl), desiredCapabilities);
        driver.get(mobilebrowser.getBaseUrl());
        return driver;
    }
}
