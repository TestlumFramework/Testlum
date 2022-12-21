package com.knubisoft.cott.testing.framework.util;

import com.browserstack.local.Local;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.MutableCapabilities;

import java.util.Collections;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider.getBrowserStack;

@UtilityClass
public class BrowserStackUtil {
    private static final Local BS_LOCAL = new Local();

    public String getBrowserStackUrl() {
        if (Objects.nonNull(getBrowserStack())) {
            return "https://" + getBrowserStack().getUsername() + ":" + getBrowserStack().getPassword()
                    + "@hub-cloud.browserstack.com/wd/hub";
        }
        return "BrowserStack is empty";
    }

    public void startLocalServer(final MutableCapabilities desiredCapabilities) {
        desiredCapabilities.setCapability("browserstack.local", "true");
        try {
            BS_LOCAL.start(Collections.singletonMap("key", getBrowserStack().getPassword()));
        } catch (Exception e) {
            throw new DefaultFrameworkException();
        }
    }

    public void closeLocalServer() {
        try {
            BS_LOCAL.stop();
        } catch (Exception e) {
            throw new DefaultFrameworkException();
        }
    }
}
