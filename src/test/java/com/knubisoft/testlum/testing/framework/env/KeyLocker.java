package com.knubisoft.testlum.testing.framework.env;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static java.util.Objects.isNull;

public class KeyLocker {

    private final ConcurrentHashMap<Object, LockWrapper> keyToLocks = new ConcurrentHashMap<>();

    public boolean tryLock(final Object key, final int lockLimit) {
        LockWrapper lockWrapper = keyToLocks.compute(key, (k, lock) ->
                isNull(lock) ? new LockWrapper(lockLimit) : lock);
        return lockWrapper.lock.tryAcquire();
    }

    public void releaseLock(final Object key) {
        LockWrapper lockWrapper = keyToLocks.get(key);
        if (isNull(lockWrapper)) {
            throw new DefaultFrameworkException("Lock for key with name <%s> not found", key);
        }
        lockWrapper.lock.release();
    }

    private static class LockWrapper {
        private final Semaphore lock;

        private LockWrapper(final int lockLimit) {
            this.lock = new Semaphore(Math.max(lockLimit, 1));
        }
    }
}
