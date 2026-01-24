package com.knubisoft.testlum.testing.framework.env;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EnvManager {

    private static final ThreadLocal<Environment> THREAD_ENV = new ThreadLocal<>();

    private final Environment environment;

    public EnvManager(final List<Environment> environments) {
        this.environment = environments.stream().findFirst().get();
    }

    public static String currentEnv() {
        Environment env = THREAD_ENV.get();
        if (env == null) {
            env = ConfigProviderImpl.GlobalTestConfigurationProvider.
                    getEnabledEnvironments().stream().findFirst().get();
            THREAD_ENV.set(env);
        }
        return env.getFolder();
    }

    public String acquireEnv() throws InterruptedException {
        THREAD_ENV.set(environment);
        return currentEnv();
    }

    public void releaseEnv(final String env) {
        //THREAD_ENV.remove();
    }
}
