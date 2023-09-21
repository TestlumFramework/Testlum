package com.knubisoft.testlum.testing.framework;

import lombok.Getter;

@Getter
public class SystemInfo {

    public static final boolean TESTING_IN_PIPELINE = testingInPipeline();

    public static final boolean USE_SELENIUM_HUB = useSeleniumHub();

    private static boolean useSeleniumHub() {
        return "true".equalsIgnoreCase(System.getProperty("USE_SELENIUM_HUB"));
    }

    private static boolean testingInPipeline() {
        return "true".equalsIgnoreCase(System.getProperty("TESTING_IN_PIPELINE"));
    }
}
