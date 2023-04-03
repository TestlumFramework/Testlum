package com.knubisoft.cott.testing.framework.env;

import com.knubisoft.cott.testing.model.global_config.Environment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EnvManager {

    private static final ThreadLocal<Environment> THREAD_ENV = new ThreadLocal<>();

    private final List<Environment> environments;
    private final KeyLocker keyLocker;
    private final Lock lock;
    private final Condition lockCondition;

    public EnvManager(final List<Environment> environments) {
        this.environments = Collections.unmodifiableList(environments);
        this.keyLocker = new KeyLocker();
        this.lock = new ReentrantLock();
        this.lockCondition = lock.newCondition();
    }

    public static String currentEnv() {
        return THREAD_ENV.get().getFolder();
    }

    public String acquireEnv() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                Optional<Environment> env = tryToLockEnv();
                if (env.isPresent()) {
                    return env.get().getFolder();
                }
                lockCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    private Optional<Environment> tryToLockEnv() {
        return environments.stream()
                .filter(env -> keyLocker.tryLock(env.getFolder(), env.getThreads()))
                .findFirst()
                .map(env -> {
                    THREAD_ENV.set(env);
                    return env;
                });
    }

    public void releaseEnv(final String env) {
        lock.lock();
        try {
            keyLocker.releaseLock(env);
            THREAD_ENV.remove();
        } finally {
            try {
                lockCondition.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}
