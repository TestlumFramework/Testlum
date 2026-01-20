package com.knubisoft.testlum.testing.framework;

public class SystemPropertiesHelper {

    private SystemPropertiesHelper() {
        // nope
    }

    public static boolean isTestingInPipeline() {
        return "true".equalsIgnoreCase(System.getProperty("TESTING_IN_PIPELINE"));
    }
}
