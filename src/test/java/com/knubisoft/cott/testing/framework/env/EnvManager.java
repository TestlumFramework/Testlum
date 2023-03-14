package com.knubisoft.cott.testing.framework.env;

import com.knubisoft.cott.testing.framework.util.LogUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EnvManager {

    private static final ThreadLocal<String> THREAD_ENV = new ThreadLocal<>();

    private final List<String> environments;

    private final KeyLocker keyLocker = new KeyLocker();
    private final Lock lock = new ReentrantLock();
    private final Condition lockCondition = lock.newCondition();

    public EnvManager(final List<String> environments) {
        this.environments = Collections.unmodifiableList(environments);
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

    //todo
    private Optional<String> tryToLockEnv() {
        return environments.stream()
                .filter(keyLocker::tryLock)
                .findFirst()
                .map(env -> {
                    THREAD_ENV.set(env);
                    LogUtil.logAlias("!!!!!!!!=" + env + "=!!!! " + Thread.currentThread().getName());
                    return env;
                });
    }

    public void releaseEnv(final String env) {
        lock.lock();
        try {
            keyLocker.releaseLock(env);
            THREAD_ENV.remove();
            LogUtil.logAlias("!!!!fin=" + env + "=!!!! " + Thread.currentThread().getName());
        } finally {
            lockCondition.signal();
            lock.unlock();
        }
    }
}
