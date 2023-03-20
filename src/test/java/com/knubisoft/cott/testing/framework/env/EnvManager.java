package com.knubisoft.cott.testing.framework.env;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EnvManager {

    private static final ThreadLocal<String> THREAD_ENV = new ThreadLocal<>();

    private final List<String> environments;
    private final KeyLocker keyLocker;
    private final Lock lock;
    private final Condition lockCondition;

    public EnvManager(final List<String> environments, final int threads) {
        this.environments = Collections.unmodifiableList(environments);
        this.keyLocker = new KeyLocker(threads);
        this.lock = new ReentrantLock();
        this.lockCondition = lock.newCondition();
    }

    public static String currentEnv() {
        return THREAD_ENV.get();
    }

    public String acquireEnv() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                Optional<String> env = tryToLockEnv();
                if (env.isPresent()) {
                    return env.get();
                }
                lockCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    private Optional<String> tryToLockEnv() {
        return environments.stream()
                .filter(keyLocker::tryLock)
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
