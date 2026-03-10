package com.knubisoft.testlum.testing.framework.env;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;

public class EnvManager {

    private static final ThreadLocal<String> THREAD_ENV = new ThreadLocal<>();

    private EnvManager() {
        // hidden
    }

    public static String currentEnv() {
        String environment = THREAD_ENV.get();
        if (environment == null) {
            throw new DefaultFrameworkException("Current environment is not set");
        }
        return environment;
    }

    public static void setCurrentEnv(final String environment) {
        THREAD_ENV.set(environment);
    }

    public static void clearCurrentEnv() {
        THREAD_ENV.remove();
    }
}
