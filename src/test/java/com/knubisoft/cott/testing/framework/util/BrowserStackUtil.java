package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class BrowserStackUtil {

    public String getBrowserStackUrl() {
        if (Objects.nonNull(GlobalTestConfigurationProvider.getBrowserStackLogin())) {
            return "https://" + GlobalTestConfigurationProvider.getBrowserStackLogin().getUsername()
                    + ":" + GlobalTestConfigurationProvider.getBrowserStackLogin().getPassword()
                    + "@hub-cloud.browserstack.com/wd/hub";
        }
        return "BrowserStack is empty";
    }
}
