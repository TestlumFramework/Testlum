package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.Platform;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Objects;

@UtilityClass
public class MobilebrowserDriverFactory {

    public WebDriver createDriver(final MobilebrowserDevice mobileDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setDefaultCapabilities(mobileDevice, desiredCapabilities);
        setCommonCapabilities(mobileDevice, desiredCapabilities);
        setPlatformCapabilities(mobileDevice, desiredCapabilities);
        return getMobilebrowserWebDriver(desiredCapabilities);
    }

    @SneakyThrows
    private WebDriver getMobilebrowserWebDriver(final DesiredCapabilities desiredCapabilities) {
        Mobilebrowser mobilebrowserSettings = GlobalTestConfigurationProvider.getMobilebrowserSettings();
        String serverUrl = MobileDriverUtil.getServerUrl(mobilebrowserSettings.getConnection());
        WebDriver driver = new RemoteWebDriver(new URL(serverUrl), desiredCapabilities);
        driver.get(mobilebrowserSettings.getBaseUrl());
        return driver;
    }

    private void setCommonCapabilities(final MobilebrowserDevice mobileDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        if (Objects.nonNull(mobileDevice.getAppiumCapabilities())) {
            AppiumCapabilities capabilities = mobileDevice.getAppiumCapabilities();
            MobileDriverUtil.setCommonCapabilities(desiredCapabilities, mobileDevice, capabilities);
            desiredCapabilities.setCapability(MobileCapabilityType.UDID, capabilities.getUdid());
        } else if (Objects.nonNull(mobileDevice.getBrowserStackCapabilities())) {
            MobileDriverUtil.setCommonCapabilities(
                    desiredCapabilities, mobileDevice, mobileDevice.getBrowserStackCapabilities());
            desiredCapabilities.setCapability("browserstack.local", Boolean.TRUE);
        }
    }

    private void setPlatformCapabilities(final MobilebrowserDevice mobileDevice,
                                         final DesiredCapabilities desiredCapabilities) {
        if (Platform.ANDROID == mobileDevice.getPlatformName()) {
            desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "uiautomator2");
            desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
        } else if (Platform.IOS == mobileDevice.getPlatformName()) {
            desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
            desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Safari");
        } else {
            throw new DefaultFrameworkException("Unknown mobile platform name: ", mobileDevice.getPlatformName());
        }
    }
}
