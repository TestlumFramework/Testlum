package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.Capabilities;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.remote.DesiredCapabilities;

@UtilityClass
public class MobileDriverUtil {
    public void setCommonCapabilities(final AbstractDevice abstractDevice,
                                      final DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, abstractDevice.getDeviceName());
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "5000");
        setAdditionalCapabilities(abstractDevice, desiredCapabilities);
    }

    public void setAutomation(final DesiredCapabilities desiredCapabilities,
                              final String platform,
                              final String automation) {
        desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, automation);
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, platform);
    }

    private void setAdditionalCapabilities(final AbstractDevice device, final DesiredCapabilities desiredCapabilities) {
        Capabilities capabilities = device.getCapabilities();
        if (capabilities != null) {
            capabilities.getCapability()
                    .forEach(cap -> desiredCapabilities.setCapability(cap.getCapabilityName(), cap.getValue()));
        }
    }

}
