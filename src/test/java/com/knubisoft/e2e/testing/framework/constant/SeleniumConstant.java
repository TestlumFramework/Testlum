package com.knubisoft.e2e.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeleniumConstant {
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final String REMOTE_URL = "http://localhost:4444/wd/hub";
    public static final String PROFILE = "spring.profiles.active";
    public static final String USE_SELENIUM_HUB = "USE_SELENIUM_HUB";
}
