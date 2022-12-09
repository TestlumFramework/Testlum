package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
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
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(mobilebrowserDevice, desiredCapabilities);
        setPlatformCapabilities(mobilebrowserDevice, desiredCapabilities);
        WebDriver driver = new AppiumDriver(new URL(mobilebrowser.getAppiumServerUrl()), desiredCapabilities);
        driver.get(mobilebrowser.getBaseUrl());
        return driver;
    }

    private void setPlatformCapabilities(final MobilebrowserDevice abstractDevice,
                                         final DesiredCapabilities desiredCapabilities) {
        switch (abstractDevice.getPlatformName()) {
            case ANDROID:
                MobileDriverUtil.setAutomation(desiredCapabilities, "Android", "uiautomator2");
                desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
                break;
            case IOS:
                MobileDriverUtil.setAutomation(desiredCapabilities, "iOS", "XCUITest");
                desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Safari");
                break;
            default:
                throw new DefaultFrameworkException("The mobile platform name is undefined");

        }
    }
}
