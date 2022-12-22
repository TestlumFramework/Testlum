package com.knubisoft.cott.testing.framework.util;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.MutableCapabilities;

import java.util.Objects;

import static com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider.getBrowserStack;

@UtilityClass
public class BrowserStackUtil {

    public String getBrowserStackUrl() {
        if (Objects.nonNull(getBrowserStack())) {
            return "https://" + getBrowserStack().getUsername() + ":" + getBrowserStack().getPassword()
                    + "@hub-cloud.browserstack.com/wd/hub";
        }
        return "BrowserStack is empty";
    }

    public void startLocalServer(final MutableCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("browserstack.local", "true");
    }
}
