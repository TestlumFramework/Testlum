package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.model.global_config.Mac;
import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

@UtilityClass
public class MacDriverFactory {
    @SneakyThrows
    public Mac2Driver createDriver(final Mac mac) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "mac");
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "mac2");
        capabilities.setCapability("appium:bundleId", mac.getBundleId());
        return new Mac2Driver(new URL(mac.getAppiumServerUrl()), capabilities);
    }
}
