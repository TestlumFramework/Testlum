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
import java.util.HashMap;

import static com.knubisoft.cott.testing.framework.constant.BrowserStackConstant.BROWSER_STACK;
import static com.knubisoft.cott.testing.framework.constant.BrowserStackConstant.BROWSER_STACK_CONNECTION;
import static com.knubisoft.cott.testing.framework.constant.BrowserStackConstant.BROWSER_STACK_URL;

@UtilityClass
public class NativeDriverFactory {

    @SneakyThrows
    public WebDriver createDriver(final NativeDevice nativeDevice) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        MobileDriverUtil.setCommonCapabilities(nativeDevice, desiredCapabilities);
        String serverUrl = GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl();
        if (BROWSER_STACK_CONNECTION) {
            setBrowserStackCaps(desiredCapabilities);
        }
        if (nativeDevice instanceof IosDevice) {
            setIosCaps((IosDevice) nativeDevice, desiredCapabilities);
            return new IOSDriver(new URL(serverUrl), desiredCapabilities);
        }
        setAndroidCaps((AndroidDevice) nativeDevice, desiredCapabilities);
        return new AndroidDriver(new URL(BROWSER_STACK_CONNECTION ? BROWSER_STACK_URL : serverUrl),
                desiredCapabilities);
    }

    private static void setAndroidCaps(final AndroidDevice nativeDevice,
                                       final DesiredCapabilities desiredCapabilities) {
        MobileDriverUtil.setAutomation(desiredCapabilities, "Android", "uiautomator2");
        desiredCapabilities.setCapability(MobileCapabilityType.APP, nativeDevice.getApp());
        desiredCapabilities.setCapability("appPackage", nativeDevice.getAppPackage());
        desiredCapabilities.setCapability("appActivity", nativeDevice.getAppActivity());
    }

    private void setIosCaps(final IosDevice nativeDevice, final DesiredCapabilities desiredCapabilities) {
        MobileDriverUtil.setAutomation(desiredCapabilities, "iOS", "XCUITest");
        desiredCapabilities.setCapability(MobileCapabilityType.APP, nativeDevice.getApp());
    }

    private void setBrowserStackCaps(final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("browserstack.appStoreConfiguration", new HashMap<String, String>() {{
            put("username", BROWSER_STACK.getPlayMarketLogin().getUsername());
            put("password", BROWSER_STACK.getPlayMarketLogin().getPassword());
        }});
        desiredCapabilities.setCapability("browserstack.local", "true");
    }

}
