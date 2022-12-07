package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.MobileDriverUtil;
import com.knubisoft.cott.testing.model.global_config.AndroidDevice;
import com.knubisoft.cott.testing.model.global_config.BrowserStack;
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
    public WebDriver createDriver(final NativeDevice nativeDevice, BrowserStack browserStack) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(nativeDevice, desiredCapabilities);
        String serverUrl = GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl();

        if (GlobalTestConfigurationProvider.getNativeSettings().isBrowserStackConnectionEnabled()){
            MobileDriverUtil.setBrowserStackConnection(desiredCapabilities, browserStack);
            serverUrl = "https://"+browserStack.getBrowserStackLogin().getUsername()+
                    ":"+browserStack.getBrowserStackLogin().getPassword()+"@hub-cloud.browserstack.com/wd/hub";
        }
        if (nativeDevice instanceof IosDevice) {
            MobileDriverUtil.setAutomation(desiredCapabilities, "iOS", "XCUITest");
            desiredCapabilities.setCapability(MobileCapabilityType.APP, ((IosDevice) nativeDevice).getApp());
            return new IOSDriver(new URL(serverUrl), desiredCapabilities);
        }
        AndroidDevice device = (AndroidDevice) nativeDevice;
        MobileDriverUtil.setAutomation(desiredCapabilities, "Android", "uiautomator2");
        desiredCapabilities.setCapability("app", "bs://5c179d138212f08d02c86bfbfb1385a739fd13d7");
        desiredCapabilities.setCapability("appPackage", device.getAppPackage());
        desiredCapabilities.setCapability("appActivity", device.getAppActivity());
        return new AndroidDriver(new URL(serverUrl), desiredCapabilities);
    }

}
