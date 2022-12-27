package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.BrowserStackLogin;
import lombok.experimental.UtilityClass;

import java.util.Objects;

import static java.lang.String.format;

@UtilityClass
public class BrowserStackUtil {
    private static final BrowserStackLogin BROWSER_STACK_LOGIN = GlobalTestConfigurationProvider.getBrowserStackLogin();
    private static final String BROWSER_STACK_URL_TEMPLATE = "https://%s:%s@hub-cloud.browserstack.com/wd/hub";

    public String getBrowserStackUrl() {
        if (Objects.nonNull(BROWSER_STACK_LOGIN)) {
            return format(BROWSER_STACK_URL_TEMPLATE,
                    BROWSER_STACK_LOGIN.getUsername(),
                    BROWSER_STACK_LOGIN.getPassword());
        }
        throw new DefaultFrameworkException("Cannot find BrowserStackLogin configuration");
    }
}
