package com.knubisoft.cott.testing.framework.env;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static java.util.Objects.isNull;

public class KeyLocker {

    private final ConcurrentHashMap<Object, LockWrapper> keyToLocks = new ConcurrentHashMap<>();

    public boolean tryLock(final Object key) {
        LockWrapper lockWrapper = keyToLocks.compute(key, (k, lock) ->
                isNull(lock) ? new LockWrapper() : lock);
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
        //todo change permits by parallelism config
        private final Semaphore lock = new Semaphore(1);
    }
}
