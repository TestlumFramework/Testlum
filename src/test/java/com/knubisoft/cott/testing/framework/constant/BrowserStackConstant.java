package com.knubisoft.cott.testing.framework.constant;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.BrowserStack;

public class BrowserStackConstant {
    public static final BrowserStack BROWSER_STACK = GlobalTestConfigurationProvider.getBrowserStack();
    public static final String BROWSER_STACK_URL = "https://" + BROWSER_STACK.getBrowserStackLogin().getUsername()
            + ":" + BROWSER_STACK.getBrowserStackLogin().getPassword() + "@hub-cloud.browserstack.com/wd/hub";
    public static final boolean BROWSER_STACK_NATIVE_CONNECTION =
            GlobalTestConfigurationProvider.getNativeSettings().isBrowserStackConnectionEnabled();
    public static final boolean BROWSER_STACK_NATIVE_WEB_CONNECTION =
            GlobalTestConfigurationProvider.getMobilebrowserSettings().isBrowserStackConnectionEnabled();
    public static final boolean BROWSER_STACK_WEB_CONNECTION =
            GlobalTestConfigurationProvider.getBrowserSettings().isBrowserStackConnectionEnabled();
}
