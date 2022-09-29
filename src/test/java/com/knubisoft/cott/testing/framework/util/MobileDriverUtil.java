package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.remote.DesiredCapabilities;

@UtilityClass
public class MobileDriverUtil {
    public void setCommonCapabilities(AbstractDevice abstractDevice, DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, abstractDevice.getDeviceName());
        desiredCapabilities.setCapability("udid", abstractDevice.getUdid());
        setPlatformCapabilities(abstractDevice, desiredCapabilities);
    }

    private void setPlatformCapabilities(AbstractDevice abstractDevice, DesiredCapabilities desiredCapabilities) {
        switch (abstractDevice.getPlatformName()) {
            case ANDROID:
                desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "uiautomator2");
                desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
                if (abstractDevice instanceof MobilebrowserDevice) {
                    desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
                }
                break;
            case IOS:
                desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
                desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "iOS");
                if (abstractDevice instanceof MobilebrowserDevice) {
                    desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Safari");
                }
                break;
            default:
                throw new DefaultFrameworkException("The mobile platform name is undefined");

        }
    }
}
